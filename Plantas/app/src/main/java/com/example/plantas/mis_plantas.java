package com.example.plantas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class mis_plantas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private PlantasAdapter plantasAdapter;
    private List<Planta> plantasList;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_plantas);

        recyclerView = findViewById(R.id.recycler_view_plantas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", MODE_PRIVATE);
        uid = sharedPreferences.getString("uid", null);

        plantasList = new ArrayList<>();
        plantasAdapter = new PlantasAdapter(plantasList, this);
        recyclerView.setAdapter(plantasAdapter);

        // Llamar a cargarPlantas cuando se crea la actividad
        cargarPlantas();

        ImageView btnRecargar = findViewById(R.id.recargar);
        btnRecargar.setOnClickListener(v -> {
            cargarPlantas();
        });

        FloatingActionButton fab = findViewById(R.id.btn_agregar_planta);
        fab.setOnClickListener(view -> {
            Ventana_agregar_planta bottomSheet = new Ventana_agregar_planta(this);
            bottomSheet.show(getSupportFragmentManager(), "Ventana_agregar_planta");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                boolean recargarLista = data.getBooleanExtra("recargar_lista", false);
                if (recargarLista) {
                    cargarPlantas(); // Recargar la lista de plantas
                }
            }
        }
    }

    public void cargarPlantas() {
        if (uid != null) {
            db.collection(uid).document("plantas")
                    .collection("mis_plantas")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                plantasList.clear(); // Asegúrate de limpiar la lista siempre
                                if (!querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        Planta planta = document.toObject(Planta.class);
                                        if (planta != null) {
                                            plantasList.add(planta);
                                        } else {
                                            Toast.makeText(this, "Error: Documento no se pudo convertir a Planta", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "No hay plantas registradas.", Toast.LENGTH_SHORT).show();
                                }
                                plantasAdapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
                            }
                        } else {
                            Toast.makeText(this, "Error en la consulta: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "UID es nulo, no se puede cargar las plantas.", Toast.LENGTH_SHORT).show();
        }
    }


}
