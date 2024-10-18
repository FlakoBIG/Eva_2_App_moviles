package com.example.plantas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Fotos_planta extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;

    private String plantaId, uid;

    private RecyclerView recyclerFotos;
    private FotoAdapter fotoAdapter;
    private List<String> listaFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos_planta);

        plantaId = getIntent().getStringExtra("plantaId");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();


        // Cambia a GridLayoutManager para dos columnas
        recyclerFotos = findViewById(R.id.recycler_fotos);
        recyclerFotos.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        listaFotos = new ArrayList<>();
        fotoAdapter = new FotoAdapter(this, listaFotos);
        recyclerFotos.setAdapter(fotoAdapter);

        cargarFotos();

        ImageButton btnSubirFoto = findViewById(R.id.btn_subir_foto);
        Button btnAgregarPlanta = findViewById(R.id.btn_agregar_planta);

        btnSubirFoto.setOnClickListener(v -> seleccionarImagen());

        btnAgregarPlanta.setOnClickListener(v -> subirFoto());
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ImageButton btnSubirFoto = findViewById(R.id.btn_subir_foto);
                btnSubirFoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void subirFoto() {

        if (imageUri != null) {
            StorageReference fileReference = storageRef.child("imagenes/" + System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Toast.makeText(this, "Subiendo Foto...", Toast.LENGTH_SHORT).show();
                    db.collection(uid)
                            .document("plantas")
                            .collection("mis_plantas")
                            .document(plantaId)
                            .update("fotos", FieldValue.arrayUnion(downloadUrl))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(Fotos_planta.this, "Foto subida con éxito", Toast.LENGTH_SHORT).show();
                                // Actualiza la lista de fotos y recarga
                                cargarFotos(); // Carga las fotos nuevamente
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(Fotos_planta.this, "Error al guardar el enlace: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                });
            }).addOnFailureListener(e ->
                    Toast.makeText(Fotos_planta.this, "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } else {
            Toast.makeText(this, "Por favor selecciona una foto antes de subir", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarFotos() {
        listaFotos.clear(); // Limpiar la lista antes de cargar nuevas fotos
        db.collection(uid)
                .document("plantas")
                .collection("mis_plantas")
                .document(plantaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> fotos = (List<String>) documentSnapshot.get("fotos");
                        if (fotos != null) {
                            listaFotos.addAll(fotos);
                        }
                        fotoAdapter.notifyDataSetChanged(); // Notificar cambios en el adaptador
                    } else {
                        Toast.makeText(this, "No se encontraron fotos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar fotos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
