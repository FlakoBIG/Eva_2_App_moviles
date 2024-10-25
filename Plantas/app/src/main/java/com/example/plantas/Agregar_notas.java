package com.example.plantas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class Agregar_notas extends AppCompatActivity {
    private EditText editTextNota;
    private Button btnAgregar;
    private String uid;
    private String plantaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_notas);

        // Obtener datos del intent
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        plantaId = intent.getStringExtra("plantaId");

        // Inicializar vistas
        editTextNota = findViewById(R.id.editTextNota);
        btnAgregar = findViewById(R.id.btnAgregar);

        // Configurar el botón para agregar la nota
        btnAgregar.setOnClickListener(v -> guardarNota());
    }

    private void guardarNota() {
        String nota = editTextNota.getText().toString().trim();

        if (nota.isEmpty()) {
            Toast.makeText(this, "Ingresa una nota", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Referencia correcta: UID -> plantas -> mis_plantas -> plantaId -> diario -> {notas: [tuNota]}
        DocumentReference docRef = db.collection(uid)  // UID como primera colección
                .document("plantas")
                .collection("mis_plantas")
                .document(plantaId)
                .collection("diario")
                .document("notas");

        // Verificar si el documento 'diario' existe
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Si existe, agregar la nueva nota
                docRef.update("notas", FieldValue.arrayUnion(nota))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Si no existe, crear el documento con la nota inicial
                HashMap<String, Object> data = new HashMap<>();
                ArrayList<String> notas = new ArrayList<>();
                notas.add(nota);
                data.put("notas", notas);

                docRef.set(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Diario creado y nota guardada", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al crear el diario", Toast.LENGTH_SHORT).show();
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al obtener el documento", Toast.LENGTH_SHORT).show();
        });
    }
}
