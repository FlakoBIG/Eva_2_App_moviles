package com.example.plantas.ui.slideshow;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantas.Foto_amigo_Adapter;
import com.example.plantas.R;
import com.example.plantas.databinding.FragmentSlideshowBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlideshowFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentSlideshowBinding binding;
    private Uri imagenUri;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;

    private String uid;
    private RecyclerView recyclerFotos;
    private Foto_amigo_Adapter fotoAdapter;
    private List<String> listaFotos;
    private List<String> documentIds;
    private List<Integer> listaLikes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();

        SharedPreferences preferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);

        recyclerFotos = binding.recyclerViewFotos;
        recyclerFotos.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        listaFotos = new ArrayList<>();
        documentIds = new ArrayList<>();
        listaLikes = new ArrayList<>();
        fotoAdapter = new Foto_amigo_Adapter(getActivity(), listaFotos, documentIds, listaLikes, uid);
        recyclerFotos.setAdapter(fotoAdapter);

        cargarFotos();

        binding.btnSubirFoto.setOnClickListener(v -> abrirGaleria());

        binding.btnAgregarPlanta.setOnClickListener(v -> {
            String nombrePlanta = binding.etPlantName.getText().toString();
            if (!nombrePlanta.isEmpty() && imagenUri != null) {
                subirPlantaAFirebase(nombrePlanta, imagenUri);
            } else {
                Toast.makeText(getActivity(), "porfa, ingresa el nombre y selecciona una imagen", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMiCodigo.setOnClickListener(v -> {
            if (uid != null) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UID", uid);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "codigo copiado", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el boton de refrescar
        binding.btnRefresh.setOnClickListener(v -> cargarFotos());

        return root;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imagenUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagenUri);
                binding.btnSubirFoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void subirPlantaAFirebase(String nombre, Uri imagenUri) {
        if (uid == null) {
            Toast.makeText(getActivity(), "error: no se encontro el uid del usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference fotoReferencia = storageRef.child("plantas/" + uid + "/Fotos_publica/" + nombre + ".jpg");

        fotoReferencia.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
            fotoReferencia.getDownloadUrl().addOnSuccessListener(uri -> {
                String urlFoto = uri.toString();

                Map<String, Object> planta = new HashMap<>();
                planta.put("nombre", nombre);
                planta.put("foto_principal", urlFoto);

                db.collection(uid)
                        .document("plantas")
                        .collection("Fotos_publica")
                        .add(planta)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getActivity(), "foto publicada", Toast.LENGTH_SHORT).show();
                            cargarFotos();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "error al publicar la foto", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "error al subir la imagen", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarFotos() {
        listaFotos.clear();
        documentIds.clear();
        listaLikes.clear();
        db.collection(uid)
                .document("plantas")
                .collection("Fotos_publica")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String fotoUrl = document.getString("foto_principal");
                            String documentId = document.getId();
                            if (fotoUrl != null) {
                                listaFotos.add(fotoUrl);
                                documentIds.add(documentId);
                                Long likes = document.getLong("likes");
                                listaLikes.add(likes != null ? likes.intValue() : 0);
                            }
                        }
                        fotoAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "no se encontraron fotos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "error al cargar fotos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
