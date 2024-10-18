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

import com.example.plantas.FotoAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Galeria_otros extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText codigoText;
    private Button buscarGaleria;
    private RecyclerView recyclerViewFotos;
    private FotoAdapter fotoAdapter;
    private List<String> listaFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria_otros); // Asegúrate de que el layout se llama así

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar componentes de la interfaz
        codigoText = findViewById(R.id.codigo_text); // ID del EditText
        buscarGaleria = findViewById(R.id.Buscar_galeria); // ID del botón
        recyclerViewFotos = findViewById(R.id.recyclerViewFotos); // ID del RecyclerView

        // Configurar el RecyclerView
        recyclerViewFotos.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas
        listaFotos = new ArrayList<>();
        fotoAdapter = new FotoAdapter(this, listaFotos);
        recyclerViewFotos.setAdapter(fotoAdapter);

        // Configurar el botón buscar
        buscarGaleria.setOnClickListener(v -> buscarFotos());
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
        db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String fotoUrl = document.getString("foto_principal");
                            if (fotoUrl != null) {
                                listaFotos.add(fotoUrl);
                            }
                        }
                        fotoAdapter.notifyDataSetChanged(); // Notificar cambios en el adaptador
                    } else {
                        Toast.makeText(this, "El usuario no tiene fotos ingresadas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar fotos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
