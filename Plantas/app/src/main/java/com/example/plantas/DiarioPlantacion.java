package com.example.plantas;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


public class DiarioPlantacion extends AppCompatActivity {

    private RecyclerView recyclerViewNotas, recyclerViewProblemas;
    private NotaAdapter notaAdapter;
    private ProblemaAdapter problemaAdapter;
    private List<String> listaNotas = new ArrayList<>();
    private List<Map<String, String>> listaProblemas = new ArrayList<>(); // Asegúrate de que sea de tipo Map
    private String uid;
    private String plantaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario_plantacion);

        // Obtener datos del intent
        uid = getIntent().getStringExtra("uid");
        plantaId = getIntent().getStringExtra("plantaId");

        // Inicializar RecyclerViews
        recyclerViewNotas = findViewById(R.id.recyclerViewNotas);
        recyclerViewProblemas = findViewById(R.id.recyclerViewProblemas);

        recyclerViewNotas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProblemas.setLayoutManager(new LinearLayoutManager(this));

        // Cargar notas y problemas
        cargarNotas();
        cargarProblemas();
    }

    private void cargarNotas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(uid)
                .document("plantas")
                .collection("mis_plantas")
                .document(plantaId)
                .collection("diario")
                .document("notas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            List<String> notas = (List<String>) document.get("notas");
                            if (notas != null) {
                                listaNotas.addAll(notas);
                                notaAdapter = new NotaAdapter(listaNotas);
                                recyclerViewNotas.setAdapter(notaAdapter);
                            } else {
                                Toast.makeText(this, "No hay notas disponibles", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No se encontraron notas", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar notas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarProblemas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(uid)
                .document("plantas")
                .collection("mis_plantas")
                .document(plantaId)
                .collection("problema_solucion")
                .document("registro")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            List<HashMap<String, String>> problemas = (List<HashMap<String, String>>) document.get("registros");
                            if (problemas != null) {
                                listaProblemas.addAll(problemas);
                                problemaAdapter = new ProblemaAdapter(listaProblemas);
                                recyclerViewProblemas.setAdapter(problemaAdapter);
                            } else {
                                Toast.makeText(this, "No hay problemas registrados", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No se encontraron problemas", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar problemas", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
