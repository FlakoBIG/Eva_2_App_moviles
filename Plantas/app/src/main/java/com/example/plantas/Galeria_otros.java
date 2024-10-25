package com.example.plantas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Galeria_otros extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText codigoText;
    private Button buscarGaleria;
    private Button btnRefresh; // Agregar el botón de recarga
    private RecyclerView recyclerViewFotos;
    private Foto_amigo_Adapter fotoAmigoAdapter; // Corrige el nombre aquí
    private List<String> listaFotos;
    private List<String> documentIds; // Agregar esta lista para los IDs de documentos
    private List<Integer> listaLikes; // Lista para almacenar la cantidad de likes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria_otros);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar componentes de la interfaz
        codigoText = findViewById(R.id.codigo_text); // ID del EditText
        buscarGaleria = findViewById(R.id.Buscar_galeria); // ID del botón
        btnRefresh = findViewById(R.id.btn_refresh); // ID del botón de recarga
        recyclerViewFotos = findViewById(R.id.recyclerViewFotos); // ID del RecyclerView

        // Configurar el RecyclerView
        recyclerViewFotos.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas
        listaFotos = new ArrayList<>();
        documentIds = new ArrayList<>(); // Inicializar la lista de IDs de documentos
        listaLikes = new ArrayList<>(); // Inicializar la lista de likes
        fotoAmigoAdapter = new Foto_amigo_Adapter(this, listaFotos, documentIds, listaLikes, ""); // Pasa un UID vacío por ahora
        recyclerViewFotos.setAdapter(fotoAmigoAdapter);

        // Configurar el botón buscar
        buscarGaleria.setOnClickListener(v -> buscarFotos());

        // Configurar el botón de recarga
        btnRefresh.setOnClickListener(v -> {
            String amigoUid = codigoText.getText().toString().trim();
            if (!amigoUid.isEmpty()) {
                cargarFotos(amigoUid); // Recargar fotos solo si el amigoUid no está vacío
            } else {
                Toast.makeText(this, "Por favor, ingresa el código del amigo para recargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarFotos() {
        String amigoUid = codigoText.getText().toString().trim();
        if (amigoUid.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el código del amigo", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarFotos(amigoUid);
    }

    private void cargarFotos(String amigoUid) {
        listaFotos.clear(); // Limpiar la lista antes de cargar nuevas fotos
        documentIds.clear(); // Limpiar la lista de documentIds
        listaLikes.clear(); // Limpiar la lista de likes
        db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String fotoUrl = document.getString("foto_principal");
                            String documentId = document.getId(); // Obtener el ID del documento
                            if (fotoUrl != null) {
                                listaFotos.add(fotoUrl);
                                documentIds.add(documentId); // Agregar el ID del documento a la lista
                                // Agregar la cantidad de likes a la lista
                                Long likes = document.getLong("likes");
                                listaLikes.add(likes != null ? likes.intValue() : 0);
                            }
                        }
                        fotoAmigoAdapter.notifyDataSetChanged(); // Notificar cambios en el adaptador
                        fotoAmigoAdapter.setAmigoUid(amigoUid); // Establecer el UID del amigo en el adaptador
                    } else {
                        Toast.makeText(this, "El usuario no tiene fotos ingresadas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar fotos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
