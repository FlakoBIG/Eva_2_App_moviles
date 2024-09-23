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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Ventana_agregar_planta extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton btnSubirFoto;
    private Uri imagenUri;
    private EditText etNombrePlanta, etFechaPlantacion;
    private Button btnAgregarPlanta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflar el layout del panel deslizante
        View view = inflater.inflate(R.layout.ventana_agregar_planta, container, false);

        // inicializar los elementos de la interfaz
        btnSubirFoto = view.findViewById(R.id.btn_subir_foto);
        etNombrePlanta = view.findViewById(R.id.et_plant_name);
        etFechaPlantacion = view.findViewById(R.id.et_fecha_plantación); // edittext para la fecha
        ImageView ivCalendar = view.findViewById(R.id.iv_calendar); // icono del calendario
        btnAgregarPlanta = view.findViewById(R.id.btn_agregar_planta);

        // configurar el boton para abrir la galeria
        btnSubirFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        // configurar el boton del calendario para seleccionar la fecha
        ivCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePicker();
            }
        });

        // configurar el boton para agregar la planta a firebase
        btnAgregarPlanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombrePlanta = etNombrePlanta.getText().toString();
                String fechaPlantacion = etFechaPlantacion.getText().toString();
                Toast.makeText(getActivity(), "subiendo planta", Toast.LENGTH_SHORT).show();
                if (!nombrePlanta.isEmpty() && imagenUri != null) {
                    subirPlantaAFirebase(nombrePlanta, fechaPlantacion, imagenUri);
                } else {
                    // mostrar error si falta informacion
                    Toast.makeText(getActivity(), "por favor, ingresa el nombre y la imagen de la planta", Toast.LENGTH_SHORT).show();
                }
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
            // formatea la fecha a "DD/MM/YYYY"
            String fechaSeleccionada = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            etFechaPlantacion.setText(fechaSeleccionada); // mostrar la fecha en el edittext
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imagenUri = data.getData();
            btnSubirFoto.setImageURI(imagenUri); // mostrar la imagen en el imagebutton
        }
    }

    private void subirPlantaAFirebase(String nombre, String fechaPlantacion, Uri imagenUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // obtener el uid desde sharedpreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        String userId = preferences.getString("uid", null);

        if (userId == null) {
            Toast.makeText(getActivity(), "error: no se encontro el uid del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // crear una referencia para la subcoleccion "mis_plantas" dentro del documento "plantas"
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("usuarios/" + userId + "/plantas");

        // subir la imagen a firebase storage
        if (imagenUri != null) {
            StorageReference fotoReferencia = storageReference.child(nombre + ".jpg");
            fotoReferencia.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
                fotoReferencia.getDownloadUrl().addOnSuccessListener(uri -> {
                    // obtenemos la url de la imagen subida
                    String urlFoto = uri.toString();

                    // crear el mapa con los datos de la planta
                    Map<String, Object> planta = new HashMap<>();
                    planta.put("nombre", nombre);
                    planta.put("foto_principal", urlFoto);
                    planta.put("fecha_plantacion", fechaPlantacion);
                    planta.put("fotos", new ArrayList<String>()); // si hay mas fotos, las añades aqui

                    // guardar la planta en el documento "plantas" dentro de la coleccion con el uid
                    db.collection(userId).document("plantas")
                            .collection("mis_plantas")
                            .add(planta)
                            .addOnSuccessListener(documentReference -> {
                                // mostrar mensaje de exito
                                Toast.makeText(getActivity(), "planta registrada correctamente", Toast.LENGTH_SHORT).show();

                                // cerrar el panel deslizante despues de registrar la planta
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                // mostrar mensaje de error
                                Toast.makeText(getActivity(), "error al registrar la planta", Toast.LENGTH_SHORT).show();
                            });
                });
            });
        }
    }
}
