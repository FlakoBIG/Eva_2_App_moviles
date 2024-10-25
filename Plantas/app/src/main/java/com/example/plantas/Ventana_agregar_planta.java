package com.example.plantas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Ventana_agregar_planta extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton btnSubirFoto, btnVerPlantas;
    private Uri imagenUri;
    private EditText etNombrePlanta, etFechaPlantacion;
    private Button btnAgregarPlanta;
    private String nombreRealPlantaSeleccionada; // Campo para almacenar el nombre real de la planta seleccionada
    private mis_plantas actividadPrincipal;

    public Ventana_agregar_planta(mis_plantas actividadPrincipal) {
        this.actividadPrincipal = actividadPrincipal;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ventana_agregar_planta, container, false);

        btnSubirFoto = view.findViewById(R.id.btn_subir_foto);
        etNombrePlanta = view.findViewById(R.id.et_plant_name);
        etFechaPlantacion = view.findViewById(R.id.et_fecha_plantación);
        ImageView ivCalendar = view.findViewById(R.id.iv_calendar);
        btnAgregarPlanta = view.findViewById(R.id.btn_agregar_planta);
        btnVerPlantas = view.findViewById(R.id.plantita); // icono para ver la lista de plantas

        btnSubirFoto.setOnClickListener(v -> abrirGaleria());
        ivCalendar.setOnClickListener(v -> mostrarDatePicker());

        // Al presionar el icono de la planta, se abre la ventana de la lista de plantas
        btnVerPlantas.setOnClickListener(v -> mostrarListaPlantas());

        btnAgregarPlanta.setOnClickListener(v -> {
            String nombrePlanta = etNombrePlanta.getText().toString();
            String fechaPlantacion = etFechaPlantacion.getText().toString();

            if (nombreRealPlantaSeleccionada == null) {
                Toast.makeText(getActivity(), "Por favor, selecciona una planta de la lista", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!nombrePlanta.isEmpty() && imagenUri != null) {
                subirPlantaAFirebase(nombrePlanta, fechaPlantacion, imagenUri, nombreRealPlantaSeleccionada);
            } else {
                Toast.makeText(getActivity(), "Por favor, ingresa el nombre y la imagen de la planta", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String fechaSeleccionada = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            etFechaPlantacion.setText(fechaSeleccionada);
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imagenUri = data.getData();
            btnSubirFoto.setImageURI(imagenUri);
        }
    }

    private void subirPlantaAFirebase(String nombre, String fechaPlantacion, Uri imagenUri, String nombreRealPlantaSeleccionada) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        String userId = preferences.getString("uid", null);

        if (userId == null) {
            Toast.makeText(getActivity(), "Error: no se encontró el UID del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("usuarios/" + userId + "/plantas");

        if (imagenUri != null) {
            StorageReference fotoReferencia = storageReference.child(nombre + ".jpg");
            fotoReferencia.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
                fotoReferencia.getDownloadUrl().addOnSuccessListener(uri -> {
                    String urlFoto = uri.toString();

                    // Crear un mapa para la planta
                    Map<String, Object> planta = new HashMap<>();
                    planta.put("nombre", nombre);
                    planta.put("foto_principal", urlFoto);
                    planta.put("fecha_plantacion", fechaPlantacion);
                    planta.put("nombre_real", nombreRealPlantaSeleccionada);
                    planta.put("fotos", new ArrayList<String>());

                    // Agregar la planta a Firestore
                    db.collection(userId).document("plantas")
                            .collection("mis_plantas")
                            .add(planta)
                            .addOnSuccessListener(documentReference -> {
                                String plantaId = documentReference.getId();
                                planta.put("id", plantaId);

                                // Aquí se maneja el contador de plantas
                                manejarContadorNombreReal(db, userId, nombreRealPlantaSeleccionada);

                                documentReference.set(planta)
                                        .addOnSuccessListener(aVoid -> {
                                            verificarCantidadYActualizar(db, userId, nombre);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), "Error al añadir el ID a la planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Error al añadir planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error al obtener la URL de la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void manejarContadorNombreReal(FirebaseFirestore db, String userId, String nombreReal) {
        DocumentReference perfilRef = db.collection(userId).document("datos_perfil");

        perfilRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Verifica si ya existe el campo Plantas_reales_usando
                if (documentSnapshot.contains("Plantas_reales_usando")) {
                    // Obtiene la lista existente
                    Map<String, Object> plantasUsando = (Map<String, Object>) documentSnapshot.get("Plantas_reales_usando");

                    if (plantasUsando.containsKey(nombreReal)) {
                        // Si el nombre_real ya existe, incrementa el contador
                        long contador = (long) plantasUsando.get(nombreReal);
                        plantasUsando.put(nombreReal, contador + 1);
                    } else {
                        // Si no existe, inicializa el contador
                        plantasUsando.put(nombreReal, 1);
                    }
                    // Actualiza la lista en Firestore
                    perfilRef.update("Plantas_reales_usando", plantasUsando)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Error al actualizar Plantas_reales_usando: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Si no existe, crea una nueva lista y añade el nombre_real
                    Map<String, Object> nuevasPlantas = new HashMap<>();
                    nuevasPlantas.put(nombreReal, 1);
                    perfilRef.update("Plantas_reales_usando", nuevasPlantas)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Error al crear Plantas_reales_usando: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(getActivity(), "No se encontró el documento 'datos_perfil'", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error al leer datos del perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void verificarCantidadYActualizar(FirebaseFirestore db, String userId, String nombrePlanta) {
        DocumentReference jardinRef = db.collection(userId).document("datos_jardin");

        jardinRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                long cantidadActual = documentSnapshot.getLong("cantidad_de_plantas");

                // Si la cantidad de plantas es 0, se actualiza "planta_mas_antigua"
                if (cantidadActual == 0) {
                    jardinRef.update("planta_mas_antigua", nombrePlanta)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Planta mas antigua registrada", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Error al actualizar planta mas antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                // Incrementar la cantidad de plantas
                long nuevaCantidad = cantidadActual + 1;
                jardinRef.update("cantidad_de_plantas", nuevaCantidad)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), "Planta añadida y cantidad actualizada", Toast.LENGTH_SHORT).show();
                            dismiss();
                            actividadPrincipal.cargarPlantas();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Error al actualizar la cantidad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                Toast.makeText(getActivity(), "No se encontró el documento 'datos_jardin'", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error al leer datos del jardin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarListaPlantas() {
        ListaPlantasFragment listaPlantasFragment = new ListaPlantasFragment(nombrePlanta -> {
            // Guardar el nombre real de la planta seleccionada
            nombreRealPlantaSeleccionada = nombrePlanta;
            Toast.makeText(getActivity(), "Planta seleccionada: " + nombrePlanta, Toast.LENGTH_SHORT).show();
        });
        listaPlantasFragment.show(getActivity().getSupportFragmentManager(), listaPlantasFragment.getTag());
    }


}
