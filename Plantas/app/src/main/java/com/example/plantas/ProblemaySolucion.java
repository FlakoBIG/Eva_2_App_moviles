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

import java.util.HashMap;
import java.util.Map;

public class ProblemaySolucion extends AppCompatActivity {
    private EditText editTextProblema, editTextSolucion;
    private Button btnGuardar;
    private String uid, plantaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problemay_solucion);

        // Obtener datos del intent
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        plantaId = intent.getStringExtra("plantaId");


        editTextProblema = findViewById(R.id.input_problema);
        editTextSolucion = findViewById(R.id.input_solucion);
        btnGuardar = findViewById(R.id.btn_guardar);

        // Configurar el botón para guardar problema y solución
        btnGuardar.setOnClickListener(v -> guardarProblemaYSolucion());
    }

    private void guardarProblemaYSolucion() {
        String problema = editTextProblema.getText().toString().trim();
        String solucion = editTextSolucion.getText().toString().trim();

        if (problema.isEmpty() || solucion.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Referencia: UID -> plantas -> mis_plantas -> plantaId -> problema_solucion -> {registros: [{problema, solucion}]}
        DocumentReference docRef = db.collection(uid)
                .document("plantas")
                .collection("mis_plantas")
                .document(plantaId)
                .collection("problema_solucion")
                .document("registro");

        // Crear un mapa con el par problema-solución
        Map<String, String> nuevoRegistro = new HashMap<>();
        nuevoRegistro.put("problema", problema);
        nuevoRegistro.put("solucion", solucion);

        // Agregar el nuevo registro al arreglo 'registros'
        docRef.update("registros", FieldValue.arrayUnion(nuevoRegistro))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Problema y solución guardados", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Si el documento no existe, crearlo con el primer registro
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("registros", FieldValue.arrayUnion(nuevoRegistro));

                    docRef.set(data).addOnSuccessListener(aVoid2 -> {
                        Toast.makeText(this, "Registro creado y guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e2 -> {
                        Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    });
                });
    }
}
