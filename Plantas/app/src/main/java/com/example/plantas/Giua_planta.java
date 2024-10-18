package com.example.plantas;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;

public class Giua_planta extends AppCompatActivity {

    private ImageView imagenPlanta; // Para mostrar la imagen de la planta
    private TextView recomendacionesTextView; // Para mostrar las recomendaciones
    private FirebaseFirestore db; // Instancia de Firestore
    private String nombreReal; // Para almacenar el nombre real

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_giua_planta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el nombre real desde el Intent
        nombreReal = getIntent().getStringExtra("nombre_real");

        // Inicializar vistas
        imagenPlanta = findViewById(R.id.imagen_planta); // Ajusta el ID según tu XML
        recomendacionesTextView = findViewById(R.id.recomendaciones_texto); // ID del TextView para recomendaciones

        // Cargar recomendaciones y la imagen de la planta desde Firestore
        if (nombreReal != null) {
            cargarDatos(nombreReal);
        } else {
            recomendacionesTextView.setText("Nombre real no disponible.");
        }
    }

    private void cargarDatos(String nombreReal) {
        DocumentReference plantaRef = db.collection("info_planta").document(nombreReal);
        plantaRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    // Obtener recomendaciones y fotoPlanta desde Firestore
                    String recomendaciones = task.getResult().getString("recomendaciones");
                    String fotoUrl = task.getResult().getString("fotoPlanta"); // Cambiar a "fotoPlanta"

                    // Mostrar recomendaciones en el TextView
                    if (recomendaciones != null) {
                        recomendacionesTextView.setText(recomendaciones);
                    } else {
                        recomendacionesTextView.setText("No hay recomendaciones disponibles.");
                    }

                    // Cargar la imagen de la planta usando el URL recibido
                    if (fotoUrl != null) {
                        Glide.with(this)
                                .load(fotoUrl)
                                .into(imagenPlanta);
                    }

                } else {
                    recomendacionesTextView.setText("El documento no existe.");
                }
            } else {
                recomendacionesTextView.setText("Error al obtener datos: " + task.getException().getMessage());
                Toast.makeText(this, "Error al obtener datos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
