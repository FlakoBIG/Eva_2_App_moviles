package com.example.plantas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton; // Asegúrate de importar la clase
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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

        cargarPlantas();

        FloatingActionButton fab = findViewById(R.id.btn_agregar_planta);
        fab.setOnClickListener(view -> {
            Ventana_agregar_planta bottomSheet = new Ventana_agregar_planta();
            bottomSheet.show(getSupportFragmentManager(), "Ventana_agregar_planta");
        });

        ImageButton btnViewPlantita = findViewById(R.id.btn_view_plantita);
        btnViewPlantita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mis_plantas.this, View_plantita.class);
                startActivity(intent);
            }
        });
    }

    private void cargarPlantas() {
        if (uid != null) {
            db.collection(uid).document("plantas")
                    .collection("mis_plantas")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Planta planta = document.toObject(Planta.class);
                                    plantasList.add(planta);
                                }

                                plantasAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}
