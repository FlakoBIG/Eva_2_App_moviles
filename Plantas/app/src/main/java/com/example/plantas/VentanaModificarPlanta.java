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
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class VentanaModificarPlanta extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imagenUri;
    private ImageButton btnSubirFoto;
    private EditText etNombrePlanta, etFechaPlantacion;
    private ImageButton btnSeleccionarPlanta;
    private String plantaId, urlFotoActual, nombreRealPlanta; // Añadido nombreRealPlanta
    private View_plantita actividadPrincipal;
    private Button btnModificarPlanta;

    public static VentanaModificarPlanta newInstance(View_plantita actividadPrincipal, String plantaId,
                                                     String nombreActual, String fechaActual, String urlFoto) {
        VentanaModificarPlanta fragment = new VentanaModificarPlanta();
        Bundle args = new Bundle();
        args.putString("plantaId", plantaId);
        args.putString("nombreActual", nombreActual);
        args.putString("fechaActual", fechaActual);
        args.putString("urlFoto", urlFoto);
        fragment.setArguments(args);
        fragment.actividadPrincipal = actividadPrincipal;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ventana_modificar_planta, container, false);

        btnSubirFoto = view.findViewById(R.id.btn_subir_foto);
        etNombrePlanta = view.findViewById(R.id.et_plant_name);
        etFechaPlantacion = view.findViewById(R.id.et_fecha_plantacion);
        ImageView ivCalendar = view.findViewById(R.id.iv_calendar);
        btnModificarPlanta = view.findViewById(R.id.btn_modificar_planta);
        btnSeleccionarPlanta = view.findViewById(R.id.plantita);

        if (getArguments() != null) {
            plantaId = getArguments().getString("plantaId");
            String nombreActual = getArguments().getString("nombreActual");
            String fechaActual = getArguments().getString("fechaActual");
            urlFotoActual = getArguments().getString("urlFoto");

            etNombrePlanta.setText(nombreActual);
            etFechaPlantacion.setText(fechaActual);
        }

        btnSubirFoto.setOnClickListener(v -> abrirGaleria());
        ivCalendar.setOnClickListener(v -> mostrarDatePicker());

        btnSeleccionarPlanta.setOnClickListener(v -> mostrarListaPlantas());

        btnModificarPlanta.setOnClickListener(v -> {
            String nuevoNombre = etNombrePlanta.getText().toString().trim();
            String nuevaFecha = etFechaPlantacion.getText().toString().trim();

            if (!nuevoNombre.isEmpty() && !nuevaFecha.isEmpty()) {
                modificarPlantaEnFirebase(nuevoNombre, nuevaFecha, imagenUri);
            } else {
                Toast.makeText(getActivity(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {
            String fechaSeleccionada = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            etFechaPlantacion.setText(fechaSeleccionada);
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            imagenUri = data.getData();
            btnSubirFoto.setImageURI(imagenUri);
        }
    }

    private void modificarPlantaEnFirebase(String nombre, String fechaPlantacion, Uri nuevaImagenUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        String userId = preferences.getString("uid", null);

        if (userId == null) {
            Toast.makeText(getActivity(), "Error: no se encontró el UID del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference plantaRef = db.collection(userId).document("plantas")
                .collection("mis_plantas").document(plantaId);

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("nombre", nombre);
        datosActualizados.put("nombre_real", nombreRealPlanta);
        datosActualizados.put("fecha_plantacion", fechaPlantacion);

        if (nuevaImagenUri != null) {
            subirImagenYActualizar(plantaRef, datosActualizados, nuevaImagenUri);
        } else {
            actualizarPlanta(plantaRef, datosActualizados);
        }
    }

    private void subirImagenYActualizar(DocumentReference plantaRef, Map<String, Object> datos, Uri imagenUri) {
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("usuarios/" + plantaRef.getId() + "/plantas");

        StorageReference fotoReferencia = storageReference.child(plantaId + ".jpg");
        fotoReferencia.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
            fotoReferencia.getDownloadUrl().addOnSuccessListener(uri -> {
                datos.put("foto_principal", uri.toString());
                actualizarPlanta(plantaRef, datos);
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Error al obtener URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void actualizarPlanta(DocumentReference plantaRef, Map<String, Object> datos) {
        plantaRef.update(datos)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Planta modificada con éxito", Toast.LENGTH_SHORT).show();
                    dismiss();
                    actividadPrincipal.cargarNombrePlanta();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error al actualizar la planta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarListaPlantas() {
        ListaPlantasFragment listaPlantasFragment = new ListaPlantasFragment(nombrePlanta -> {
            // Guardar el nombre real de la planta seleccionada
            nombreRealPlanta = nombrePlanta; // Actualiza la variable de nombre real
            Toast.makeText(getActivity(), "Planta seleccionada: " + nombreRealPlanta, Toast.LENGTH_SHORT).show();
        });
        listaPlantasFragment.show(getActivity().getSupportFragmentManager(), listaPlantasFragment.getTag());
    }
}
