package com.example.plantas.ui.slideshow;

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
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantas.FotoAdapter;
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
    private FotoAdapter fotoAdapter;
    private List<String> listaFotos;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflar el layout usando View Binding
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();

        // Obtener el UID desde SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);

        // Configurar el RecyclerView
        recyclerFotos = binding.recyclerViewFotos; // Asegúrate de que este ID sea correcto
        recyclerFotos.setLayoutManager(new GridLayoutManager(getActivity(), 2)); // 2 columnas

        listaFotos = new ArrayList<>();
        fotoAdapter = new FotoAdapter(getActivity(), listaFotos);
        recyclerFotos.setAdapter(fotoAdapter);

        // Cargar fotos existentes
        cargarFotos();

        // Configurar el botón para subir la foto desde la galería
        binding.btnSubirFoto.setOnClickListener(v -> abrirGaleria());

        // Configurar el botón para agregar la planta a Firebase
        binding.btnAgregarPlanta.setOnClickListener(v -> {
            String nombrePlanta = binding.etPlantName.getText().toString();
            if (!nombrePlanta.isEmpty() && imagenUri != null) {
                subirPlantaAFirebase(nombrePlanta, imagenUri);
            } else {
                Toast.makeText(getActivity(), "Porfa, ingresa el nombre y selecciona una imagen", Toast.LENGTH_SHORT).show();
            }
        });

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
            // Mostrar la imagen seleccionada en el ImageButton
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagenUri);
                binding.btnSubirFoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Subir los datos de la planta y la imagen a Firebase
    private void subirPlantaAFirebase(String nombre, Uri imagenUri) {
        if (uid == null) {
            Toast.makeText(getActivity(), "Error: no se encontró el UID del usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference fotoReferencia = storageRef.child("plantas/" + uid + "/Fotos_publica/" + nombre + ".jpg");

        fotoReferencia.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
            fotoReferencia.getDownloadUrl().addOnSuccessListener(uri -> {
                String urlFoto = uri.toString();

                // Crear el mapa con los datos de la planta
                Map<String, Object> planta = new HashMap<>();
                planta.put("nombre", nombre);
                planta.put("foto_principal", urlFoto);

                // Guardar la planta en Firebase Firestore
                db.collection(uid)
                        .document("plantas")
                        .collection("Fotos_publica")
                        .add(planta)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getActivity(), "Foto publicada", Toast.LENGTH_SHORT).show();
                            cargarFotos(); // Recargar fotos después de agregar una nueva
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Error al publicar la foto", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarFotos() {
        listaFotos.clear(); // Limpiar la lista antes de cargar nuevas fotos
        db.collection(uid)
                .document("plantas")
                .collection("Fotos_publica")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.forEach(document -> {
                            String fotoUrl = document.getString("foto_principal");
                            if (fotoUrl != null) {
                                listaFotos.add(fotoUrl);
                            }
                        });
                        fotoAdapter.notifyDataSetChanged(); // Notificar cambios en el adaptador
                    } else {
                        Toast.makeText(getActivity(), "No se encontraron fotos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error al cargar fotos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
