package com.example.plantas;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue; // Para usar FieldValue
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class View_plantita extends AppCompatActivity {

    private String plantaId; // Para almacenar el ID de la planta
    private TextView nombreTextView; // Para mostrar el nombre de la planta
    private TextView textoFechaPlantacion; // Para mostrar la fecha de plantación
    private TextView nombreRealTextView; // Para mostrar el nombre real de la planta
    private ImageView imagenPlanta; // Para mostrar la imagen de la planta
    private FirebaseFirestore db; // Instancia de Firestore
    private ListenerRegistration listenerRegistration; // Para registrar el Listener
    private String uid; // Para el UID del usuario
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_plantita);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el ID de la planta desde el Intent
        plantaId = getIntent().getStringExtra("plantaId");

        // Inicializar vistas
        nombreTextView = findViewById(R.id.nombre_planta);
        textoFechaPlantacion = findViewById(R.id.texto_fecha_plantacion);
        imagenPlanta = findViewById(R.id.imagen_planta);
        nombreRealTextView = findViewById(R.id.nombre_real);

        // Obtener el UID del usuario desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("Credenciales", MODE_PRIVATE);
        uid = preferences.getString("uid", null);

        // Cargar la información de la planta
        cargarNombrePlanta(plantaId);

        Button btnGuia = findViewById(R.id.btn_guia);
        btnGuia.setOnClickListener(v -> {
            // Extraer solo el nombre real sin el prefijo
            String nombreReal = nombreRealTextView.getText().toString().replace("Nombre real: ", "").trim();
            Intent intent = new Intent(View_plantita.this, Giua_planta.class);
            intent.putExtra("nombre_real", nombreReal);
            startActivity(intent);
        });

        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Configura el botón para abrir el BottomSheet
        Button btnAgregarEntrada = findViewById(R.id.btn_agregar_entrada);
        btnAgregarEntrada.setOnClickListener(v -> {
            bottomSheetDialog.show();
        });


        Button btnFotos = findViewById(R.id.btn_fotos);
        btnFotos.setOnClickListener(v -> {
            Intent intent = new Intent(View_plantita.this, Fotos_planta.class);
            intent.putExtra("plantaId", plantaId); // Pasar el ID de la planta
            startActivity(intent);
        });

        Button btnEliminarPlanta = findViewById(R.id.btn_eliminar_planta);
        btnEliminarPlanta.setOnClickListener(v -> {
            new AlertDialog.Builder(View_plantita.this)
                    .setTitle("Eliminar Planta")
                    .setMessage("¿Estás seguro que quieres eliminar esta planta?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Mostrar segunda pregunta
                        new AlertDialog.Builder(View_plantita.this)
                                .setTitle("Planta Perecida")
                                .setMessage("¿La planta pereció?")
                                .setPositiveButton("Sí", (dialog2, which2) -> {
                                    // Incrementar cantidad_de_plantas_perecidas y eliminar planta
                                    incrementarPlantasPerecidasYEliminar();
                                })
                                .setNegativeButton("No", (dialog2, which2) -> {
                                    // Eliminar planta sin incrementar
                                    eliminarPlanta();
                                })
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistrar el Listener si está activo
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void incrementarPlantasPerecidasYEliminar() {
        DocumentReference jardinRef = db.collection(uid).document("datos_jardin");

        // Obtener la cantidad de plantas perecidas y actualizarla
        jardinRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long cantidadPerecidas = task.getResult().getLong("cantidad_de_plantas_perecidas");
                cantidadPerecidas++;

                // Actualizar la cantidad de plantas perecidas
                jardinRef.update("cantidad_de_plantas_perecidas", cantidadPerecidas)
                        .addOnSuccessListener(aVoid -> {
                            // Después de actualizar, eliminar la planta
                            eliminarPlanta();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(View_plantita.this, "Error al actualizar cantidad de plantas perecidas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Error al obtener datos del jardín: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarPlanta() {
        if (plantaId != null && uid != null) {
            DocumentReference plantaRef = db.collection(uid)
                    .document("plantas")
                    .collection("mis_plantas")
                    .document(plantaId);

            // Obtener el nombre real antes de eliminar la planta
            plantaRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombreReal = documentSnapshot.getString("nombre_real");

                    plantaRef.delete().addOnSuccessListener(aVoid -> {
                        DocumentReference jardinRef = db.collection(uid)
                                .document("datos_jardin");

                        jardinRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                int cantidadDePlantas = task.getResult().getLong("cantidad_de_plantas").intValue();
                                cantidadDePlantas--;

                                // Reducir el contador de Plantas_reales_usando
                                reducirContadorNombreReal(nombreReal);

                                if (cantidadDePlantas == 0) {
                                    jardinRef.update("cantidad_de_plantas", cantidadDePlantas, "planta_mas_antigua", "no hay plantas")
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(View_plantita.this, "Planta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent();
                                                intent.putExtra("recargar_lista", true); // Indicar que se debe recargar
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(View_plantita.this, "Error al actualizar datos del jardín: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    jardinRef.update("cantidad_de_plantas", cantidadDePlantas)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(View_plantita.this, "Planta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent();
                                                intent.putExtra("recargar_lista", true); // Indicar que se debe recargar
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(View_plantita.this, "Error al actualizar cantidad de plantas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Toast.makeText(this, "Error al obtener datos del jardín: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(View_plantita.this, "Error al eliminar la planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(View_plantita.this, "Planta no encontrada", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(View_plantita.this, "Error al obtener planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "ID de planta o UID no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void reducirContadorNombreReal(String nombreReal) {
        DocumentReference perfilRef = db.collection(uid).document("datos_perfil");

        // Reducir el contador del nombre_real
        perfilRef.update("Plantas_reales_usando." + nombreReal, FieldValue.increment(-1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Verificar si el contador llegó a 0 y eliminar si es necesario
                        perfilRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.contains("Plantas_reales_usando")) {
                                Map<String, Object> plantasUsando = (Map<String, Object>) documentSnapshot.get("Plantas_reales_usando");
                                if (plantasUsando.containsKey(nombreReal) && plantasUsando.get(nombreReal) instanceof Long) {
                                    long contador = (Long) plantasUsando.get(nombreReal);
                                    if (contador == 0) {
                                        // Eliminar el nombre_real de la lista si es 0
                                        plantasUsando.remove(nombreReal);
                                        perfilRef.update("Plantas_reales_usando", plantasUsando)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Aquí puedes agregar un mensaje o acción adicional si lo deseas
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(View_plantita.this, "Error al eliminar el nombre real: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            }
                        });
                    } else {
                        Toast.makeText(View_plantita.this, "Error al decrementar el contador: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(View_plantita.this, "Error al reducir contador en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarNombrePlanta(String plantaId) {
        if (plantaId != null && uid != null) {
            // Referencia a la planta específica
            DocumentReference plantaRef = db.collection(uid) // UID del usuario
                    .document("plantas") // Documento que contiene la colección de plantas
                    .collection("mis_plantas") // Colección que contiene las plantas
                    .document(plantaId); // ID de la planta

            listenerRegistration = plantaRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Toast.makeText(this, "Error al cargar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Obtener los datos de la planta
                    String nombre = documentSnapshot.getString("nombre");
                    String nombreReal = documentSnapshot.getString("nombre_real"); // Cargar el nombre real
                    String fechaPlantacion = documentSnapshot.getString("fecha_plantacion");
                    String fotoUrl = documentSnapshot.getString("foto_principal");

                    // Mostrar los datos en la interfaz
                    nombreTextView.setText("Nombre: " + nombre); // Agregar "Nombre: "
                    nombreRealTextView.setText("Nombre real: " + nombreReal); // Mostrar el nombre real
                    textoFechaPlantacion.setText("Fecha de Plantación: " + fechaPlantacion); // Agregar "Fecha de Plantación: "

                    // Cargar la imagen de la planta usando Glide
                    if (fotoUrl != null) {
                        Glide.with(this).load(fotoUrl).into(imagenPlanta);
                    }

                }
            });
        } else {
            Toast.makeText(this, "ID de planta o UID no disponible", Toast.LENGTH_SHORT).show();
        }
    }


}