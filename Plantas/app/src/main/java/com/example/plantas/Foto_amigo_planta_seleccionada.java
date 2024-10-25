package com.example.plantas;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class Foto_amigo_planta_seleccionada extends AppCompatActivity {

    private ImageView imageView;
    private ImageButton btnFlechaIzquierda, btnFlechaDerecha, btnLike, btnVerComentarios;
    private TextView textoNombrePlanta;
    private TextView tvLikes;

    private ArrayList<String> fotos;
    private int currentIndex;
    private int currentLikes;
    private FirebaseFirestore db;
    private String amigoUid;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_amigo_planta_seleccionada);

        // Inicialización de las vistas
        imageView = findViewById(R.id.img_cala);
        btnFlechaIzquierda = findViewById(R.id.btn_left);
        btnFlechaDerecha = findViewById(R.id.btn_right);
        btnLike = findViewById(R.id.btn_like);
        btnVerComentarios = findViewById(R.id.btn_ver_comentarios); // Botón para ver comentarios
        textoNombrePlanta = findViewById(R.id.texto_nombre_planta);
        tvLikes = findViewById(R.id.tv_likes);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener la lista de fotos y el índice actual
        fotos = getIntent().getStringArrayListExtra("fotos");
        currentIndex = getIntent().getIntExtra("current_index", 0);

        // Obtener el UID del amigo y el ID del documento
        amigoUid = getIntent().getStringExtra("amigoUid");
        documentId = getIntent().getStringExtra("documentId");

        // Cargar el nombre de la planta
        String nombrePlanta = getIntent().getStringExtra("nombre");
        textoNombrePlanta.setText(nombrePlanta != null ? nombrePlanta : "");

        // Inicializar el contador de "me gusta"
        currentLikes = 0;
        tvLikes.setText(String.valueOf(currentLikes));

        // Cargar la foto inicial
        cargarFoto();

        // Configurar los botones de navegación
        btnFlechaIzquierda.setOnClickListener(v -> cambiarFoto(-1));
        btnFlechaDerecha.setOnClickListener(v -> cambiarFoto(1));

        // Cargar el conteo de "me gusta" desde Firestore
        cargarLikes();

        // Configurar el listener para el botón de "me gusta"
        btnLike.setOnClickListener(v -> {
            // Alternar "me gusta"
            if (currentLikes > 0) {
                currentLikes--;
                btnLike.setImageResource(R.drawable.corazon_sin_relleno);
                guardarLikes(-1);
            } else {
                currentLikes++;
                btnLike.setImageResource(R.drawable.corazon_relleno);
                guardarLikes(1);
            }
            tvLikes.setText(String.valueOf(currentLikes));
        });

        // Configurar el listener para el botón de ver comentarios
        btnVerComentarios.setOnClickListener(v -> mostrarComentarios());
    }

    private void cargarFoto() {
        if (fotos != null && !fotos.isEmpty()) {
            String fotoUrl = fotos.get(currentIndex);
            Glide.with(this).load(fotoUrl).into(imageView);
        }
    }

    private void cambiarFoto(int direction) {
        currentIndex += direction;
        if (currentIndex < 0) {
            currentIndex = fotos.size() - 1;
        } else if (currentIndex >= fotos.size()) {
            currentIndex = 0;
        }
        cargarFoto();
    }

    private void cargarLikes() {
        if (documentId == null || documentId.isEmpty()) {
            Log.e("FirestoreError", "El document ID es nulo o vacío");
            return;
        }

        DocumentReference docRef = db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .document(documentId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.getLong("likes") != null) {
                currentLikes = documentSnapshot.getLong("likes").intValue();
                tvLikes.setText(String.valueOf(currentLikes));
                btnLike.setImageResource(currentLikes > 0 ? R.drawable.corazon_relleno : R.drawable.corazon_sin_relleno);
            } else {
                Log.e("FirestoreError", "El documento no existe o no tiene likes");
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Error al cargar likes: " + e.getMessage());
        });
    }

    private void guardarLikes(int change) {
        if (documentId == null || documentId.isEmpty()) {
            Log.e("FirestoreError", "El document ID es nulo o vacío");
            return;
        }

        DocumentReference docRef = db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .document(documentId);

        docRef.update("likes", FieldValue.increment(change))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Likes actualizados exitosamente"))
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error al actualizar likes: " + e.getMessage()));
    }

    private void mostrarComentarios() {
        // Crear y mostrar el diálogo de comentarios
        ComentariosDialog dialog = new ComentariosDialog(documentId, amigoUid);
        dialog.show(getSupportFragmentManager(), "ComentariosDialog");
    }

    private void cargarComentarios(ListView listViewComentarios) {
        DocumentReference docRef = db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .document(documentId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.get("comentarios") != null) {
                List<String> comentarios = (List<String>) documentSnapshot.get("comentarios");
                ComentarioAdapter adapter = new ComentarioAdapter(this, comentarios);
                listViewComentarios.setAdapter(adapter);
            }
        }).addOnFailureListener(e -> Log.e("FirestoreError", "Error al cargar comentarios: " + e.getMessage()));
    }

    private void agregarComentario(String comentario) {
        DocumentReference docRef = db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .document(documentId);

        docRef.update("comentarios", FieldValue.arrayUnion(comentario))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Comentario agregado exitosamente"))
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error al agregar comentario: " + e.getMessage()));
    }
}
