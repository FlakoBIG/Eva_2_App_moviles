package com.example.plantas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class CrearJardinActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText nombreJardinInput;
    private ImageButton camaraBoton;
    private ImageView imagenJardin;
    private Button crearJardinBoton;
    private Uri imagenUri; // uri de la imagen seleccionada

    private FirebaseFirestore db;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_jardin);

        nombreJardinInput = findViewById(R.id.editTextText);
        camaraBoton = findViewById(R.id.imageButton);
        imagenJardin = findViewById(R.id.imageView2);
        crearJardinBoton = findViewById(R.id.button);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("imagenes_jardin");

        camaraBoton.setOnClickListener(v -> abrirGaleria());
        crearJardinBoton.setOnClickListener(v -> guardarDatosJardin());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imagenUri = data.getData();
            imagenJardin.setImageURI(imagenUri); // mostrar la imagen en el imageview
        }
    }

    private void guardarDatosJardin() {
        String nombreJardin = nombreJardinInput.getText().toString().trim();

        if (nombreJardin.isEmpty()) {
            Toast.makeText(this, "por favor, ingresa un nombre para tu jardin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenUri != null) {
            // subir la imagen a firebase storage
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String urlImagen = uri.toString();
                    guardarDatosEnFirestore(nombreJardin, urlImagen); // guardar datos en firestore
                });
            }).addOnFailureListener(e -> Toast.makeText(CrearJardinActivity.this, "error al subir la imagen", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "por favor, selecciona una imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarDatosEnFirestore(String nombreJardin, String urlImagen) {
        // obtener uid de sharedpreferences
        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);

        if (uid != null) {
            Map<String, Object> datosJardin = new HashMap<>();
            datosJardin.put("Nombre", nombreJardin);
            datosJardin.put("Foto", urlImagen);
            datosJardin.put("cantidad_de_plantas", 0); // agregar cantidad de plantas inicial 0
            datosJardin.put("cantidad_de_plantas_perecidas", 0); // agregar cantidad de plantas perecidas inicial 0
            datosJardin.put("cantidad_litros_agua_gastados_mes", 0); // agregar cantidad de litros de agua gastados
            datosJardin.put("planta_mas_antigua", "ninguna planta"); // agregar la planta más antigua
            datosJardin.put("lista_de_tareas", new ArrayList<>()); // agregar una lista vacía de tareas

            // guardar datos del jardín en Firestore
            db.collection(uid).document("datos_jardin").set(datosJardin)
                    .addOnSuccessListener(aVoid -> {
                        db.collection(uid).document("datos_perfil")
                                .update("primera_vez", "no")
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(CrearJardinActivity.this, "Jardín creado con éxito", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CrearJardinActivity.this, ventanainicio.class);
                                    startActivity(intent);
                                    finish(); // cerrar la actividad actual
                                })
                                .addOnFailureListener(e -> Toast.makeText(CrearJardinActivity.this, "Error al actualizar primera_vez", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(CrearJardinActivity.this, "Error al crear el jardín", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No se encontró el uid", Toast.LENGTH_SHORT).show();
        }
    }

}
