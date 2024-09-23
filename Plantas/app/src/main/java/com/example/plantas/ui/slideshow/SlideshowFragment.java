package com.example.plantas.ui.slideshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.plantas.databinding.FragmentSlideshowBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SlideshowFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentSlideshowBinding binding;
    private Uri imagenUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflar el layout usando View Binding
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar el botón para subir la foto desde la galería
        binding.btnSubirFoto.setOnClickListener(v -> abrirGaleria());

        // Configurar el botón para subir la planta a Firebase
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

    // Abrir la galería para seleccionar una imagen
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
            binding.btnSubirFoto.setImageURI(imagenUri);
        }
    }

    // Subir los datos de la planta y la imagen a Firebase
    private void subirPlantaAFirebase(String nombre, Uri imagenUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener el UID desde SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        String userId = preferences.getString("uid", null);

        if (userId == null) {
            Toast.makeText(getActivity(), "Error: no se encontró el UID del usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una referencia para la imagen en Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("usuarios/" + userId + "/plantas");
        StorageReference fotoReferencia = storageReference.child(nombre + ".jpg");

        // Subir la imagen a Firebase Storage
        fotoReferencia.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
            fotoReferencia.getDownloadUrl().addOnSuccessListener(uri -> {
                // Obtenemos la URL de la imagen subida
                String urlFoto = uri.toString();

                // Crear el mapa con los datos de la planta
                Map<String, Object> planta = new HashMap<>();
                planta.put("nombre", nombre);
                planta.put("foto_principal", urlFoto);

                // Guardar la planta en Firebase Firestore
                db.collection(userId).document("plantas")
                        .collection("Fotos_publica")
                        .add(planta)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getActivity(), "Foto publicada", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Error al publicar la foto", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
