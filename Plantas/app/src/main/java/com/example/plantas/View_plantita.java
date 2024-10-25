package com.example.plantas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.plantas.DiarioPlantacion;


import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import android.view.View;
import android.widget.LinearLayout;
import android.content.Intent;

import java.util.Map;


public class View_plantita extends AppCompatActivity {

    private String plantaId; // Para almacenar el ID de la planta
    private TextView nombreTextView; // Para mostrar el nombre de la planta
    private TextView textoFechaPlantacion; // Para mostrar la fecha de plantacion
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

        // Cargar la informacion de la planta
        cargarNombrePlanta();

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

        // Configura el boton para abrir el BottomSheet
        Button btnAgregarEntrada = findViewById(R.id.btn_agregar_entrada);
        btnAgregarEntrada.setOnClickListener(v -> {
            bottomSheetDialog.show();
        });
        // Configurar el boton para ver diario de plantacion
        Button btnVerDiario = findViewById(R.id.ver_diario);
        btnVerDiario.setOnClickListener(v -> {
            Intent intent = new Intent(View_plantita.this, DiarioPlantacion.class);
            intent.putExtra("plantaId", plantaId);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        // Configurar el boton para agregar nota
        LinearLayout btnAgregarNota = bottomSheetView.findViewById(R.id.btn_agregar_nota);
        btnAgregarNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(View_plantita.this, Agregar_notas.class);
                intent.putExtra("plantaId", plantaId); // Envia el ID de la planta
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });
        // Cconfigurar el boton para agregar problema y solucion
        LinearLayout btnAgregarProblemaSolucion = bottomSheetView.findViewById(R.id.btn_agregar_problema_solucion);
        btnAgregarProblemaSolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(View_plantita.this, ProblemaySolucion.class);
                intent.putExtra("plantaId", plantaId); // Envia el ID de la planta
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
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
                    .setMessage("¿Estas seguro que quieres eliminar esta planta?")
                    .setPositiveButton("Si", (dialog, which) -> {
                        // Mostrar segunda pregunta
                        new AlertDialog.Builder(View_plantita.this)
                                .setTitle("Planta Perecida")
                                .setMessage("¿La planta perecio?")
                                .setPositiveButton("Si", (dialog2, which2) -> {
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

        // Boton para modificar planta
        Button btnModificarPlanta = findViewById(R.id.btn_editar);
        btnModificarPlanta.setOnClickListener(v -> {
            // Crear una nueva instancia del fragmento VentanaModificarPlanta
            VentanaModificarPlanta ventanaModificarPlanta = VentanaModificarPlanta.newInstance(
                    View_plantita.this,
                    plantaId,
                    nombreTextView.getText().toString(),
                    textoFechaPlantacion.getText().toString().replace("Fecha de plantacion: ", "").trim(),
                    // Suponiendo que estas almacenando la URL de la imagen en una variable
                    imagenPlanta.getTag() != null ? imagenPlanta.getTag().toString() : ""
            );

            // Mostrar el fragmento
            ventanaModificarPlanta.show(getSupportFragmentManager(), "ventanaModificarPlanta");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistrar el Listener si esta activo
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
                            // Despues de actualizar, eliminar la planta
                            eliminarPlanta();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(View_plantita.this, "Error al actualizar cantidad de plantas perecidas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Error al obtener datos del jardin: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarPlanta() {
        if (plantaId != null && uid != null) {
            DocumentReference plantaRef = db.collection(uid)
                    .document("plantas")
                    .collection("mis_plantas")
                    .document(plantaId);

            // Obtener el nombre de la planta antes de eliminarla
            plantaRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombrePlanta = documentSnapshot.getString("nombre"); // Cambiar a "nombre" para obtener el nombre de la planta

                    // Eliminar las subcolecciones relacionadas antes de borrar el documento principal
                    eliminarSubcolecciones(plantaRef, () -> {
                        // Después de borrar subcolecciones, eliminar la planta principal
                        plantaRef.delete().addOnSuccessListener(aVoid -> {
                            // Actualizar la cantidad de plantas en datos_jardin
                            DocumentReference jardinRef = db.collection(uid).document("datos_jardin");
                            jardinRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult().exists()) {
                                    // Usar una variable final para la cantidad de plantas
                                    final int cantidadDePlantas = task.getResult().getLong("cantidad_de_plantas").intValue();
                                    int nuevaCantidadDePlantas = cantidadDePlantas - 1; // Calcula la nueva cantidad

                                    // Reducir el contador de Plantas_reales_usando
                                    reducirContadorNombreReal(uid, nombrePlanta); // Pasar nombre de la planta aquí

                                    // Verificar si la planta eliminada es la más antigua
                                    String plantaMasAntigua = task.getResult().getString("planta_mas_antigua");
                                    if (nombrePlanta.equals(plantaMasAntigua)) {
                                        // Si es la planta más antigua, actualizar a "ninguna planta"
                                        jardinRef.update("planta_mas_antigua", "ninguna planta")
                                                .addOnSuccessListener(aVoid1 -> {
                                                    // Actualizar contador
                                                    jardinRef.update("cantidad_de_plantas", nuevaCantidadDePlantas)
                                                            .addOnSuccessListener(aVoid2 -> {
                                                                Toast.makeText(View_plantita.this, "Planta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                                                setResult(RESULT_OK);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(View_plantita.this, "Error al actualizar datos del jardin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(View_plantita.this, "Error al actualizar planta más antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Actualizar contador si no es la planta más antigua
                                        jardinRef.update("cantidad_de_plantas", nuevaCantidadDePlantas)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(View_plantita.this, "Planta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                                    setResult(RESULT_OK);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(View_plantita.this, "Error al actualizar datos del jardin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    Toast.makeText(View_plantita.this, "Error al obtener datos del jardin.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(View_plantita.this, "Error al eliminar planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    });

                }
            }).addOnFailureListener(e -> {
                Toast.makeText(View_plantita.this, "Error al obtener datos de la planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Método para eliminar subcolecciones recursivamente
    private void eliminarSubcolecciones(DocumentReference docRef, Runnable onComplete) {
        docRef.collection("fotos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Eliminar documentos en la subcolección "fotos"
                for (DocumentSnapshot doc : task.getResult()) {
                    doc.getReference().delete();
                }
                // Llamar al callback después de borrar subcolecciones
                onComplete.run();
            } else {
                Toast.makeText(View_plantita.this, "Error al eliminar subcolecciones: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para reducir el contador del nombre real
    private void reducirContadorNombreReal(String uid, String nombrePlanta) {
        DocumentReference perfilRef = db.collection(uid).document("datos_perfil");

        perfilRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("Plantas_reales_usando")) {
                    Map<String, Object> plantasUsando = (Map<String, Object>) documentSnapshot.get("Plantas_reales_usando");

                    if (plantasUsando.containsKey(nombrePlanta)) {
                        long contador = (long) plantasUsando.get(nombrePlanta);

                        // Reducir el contador en 1
                        if (contador > 1) {
                            plantasUsando.put(nombrePlanta, contador - 1);
                        } else {
                            // Si el contador es 1, eliminar la planta del mapa
                            plantasUsando.remove(nombrePlanta);
                        }

                        // Actualizar el mapa en Firestore
                        perfilRef.update("Plantas_reales_usando", plantasUsando)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(View_plantita.this, "Contador reducido correctamente.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(View_plantita.this, "Error al reducir contador del nombre real: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(View_plantita.this, "No hay datos de Plantas_reales_usando.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(View_plantita.this, "No se encontró el documento 'datos_perfil'.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(View_plantita.this, "Error al leer datos del perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }







    void cargarNombrePlanta() {
        // Obtener los datos de la planta desde Firestore
        DocumentReference plantaRef = db.collection(uid)
                .document("plantas")
                .collection("mis_plantas")
                .document(plantaId);

        listenerRegistration = plantaRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String nombre = documentSnapshot.getString("nombre");
                String fechaPlantacion = documentSnapshot.getString("fecha_plantacion");
                String urlImagen = documentSnapshot.getString("foto_principal");
                String nombreReal = documentSnapshot.getString("nombre_real");

                // Actualizar las vistas con la informacion de la planta
                nombreTextView.setText(nombre);
                textoFechaPlantacion.setText("Fecha de plantacion: " + fechaPlantacion);
                nombreRealTextView.setText("Nombre real: " + nombreReal);

                // Cargar la imagen usando Glide
                if (urlImagen != null && !urlImagen.isEmpty()) {
                    Glide.with(this)
                            .load(urlImagen)
                            .into(imagenPlanta);
                    imagenPlanta.setTag(urlImagen); // Para almacenar la URL en el tag
                }

            } else {
                Toast.makeText(this, "La planta no existe", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
