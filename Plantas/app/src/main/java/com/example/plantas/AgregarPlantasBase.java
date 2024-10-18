package com.example.plantas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class AgregarPlantasBase extends AppCompatActivity {

    private EditText nombreInput, recomendacionesInput;
    private CheckBox checkBoxEnero, checkBoxFebrero, checkBoxMarzo, checkBoxAbril, checkBoxMayo,
            checkBoxJunio, checkBoxJulio, checkBoxAgosto, checkBoxSeptiembre,
            checkBoxOctubre, checkBoxNoviembre, checkBoxDiciembre;
    private ImageView imageViewPlanta;
    private Uri imageUri; // Para almacenar la URI de la imagen seleccionada
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_plantas_base);

        // Inicializar Firestore y Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Inicializar vistas
        nombreInput = findViewById(R.id.nombre_input);
        recomendacionesInput = findViewById(R.id.recomendaciones_input);
        imageViewPlanta = findViewById(R.id.imageViewPlanta);

        // Inicializar CheckBoxes
        checkBoxEnero = findViewById(R.id.checkBoxEnero);
        checkBoxFebrero = findViewById(R.id.checkBoxFebrero);
        checkBoxMarzo = findViewById(R.id.checkBoxMarzo);
        checkBoxAbril = findViewById(R.id.checkBoxAbril);
        checkBoxMayo = findViewById(R.id.checkBoxMayo);
        checkBoxJunio = findViewById(R.id.checkBoxJunio);
        checkBoxJulio = findViewById(R.id.checkBoxJulio);
        checkBoxAgosto = findViewById(R.id.checkBoxAgosto);
        checkBoxSeptiembre = findViewById(R.id.checkBoxSeptiembre);
        checkBoxOctubre = findViewById(R.id.checkBoxOctubre);
        checkBoxNoviembre = findViewById(R.id.checkBoxNoviembre);
        checkBoxDiciembre = findViewById(R.id.checkBoxDiciembre);

        Button botonAgregarFoto = findViewById(R.id.btn_select_image);
        botonAgregarFoto.setOnClickListener(v -> abrirGaleria());

        findViewById(R.id.guardar_boton).setOnClickListener(v -> guardarPlanta());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1); // Código de solicitud 1
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageViewPlanta.setImageURI(imageUri);
            imageViewPlanta.setVisibility(View.VISIBLE);
        }
    }

    private void guardarPlanta() {
        String nombrePlanta = nombreInput.getText().toString().trim();

        if (nombrePlanta.isEmpty()) {
            Toast.makeText(this, "El nombre de la planta no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference plantasRef = db.collection("info_planta");
        plantasRef.whereEqualTo("nombre", nombrePlanta).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Toast.makeText(this, "La planta ya está registrada", Toast.LENGTH_SHORT).show();
                        } else {
                            verificarDatos(nombrePlanta);
                        }
                    } else {
                        Toast.makeText(this, "Error al verificar la planta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarDatos(String nombrePlanta) {
        String recomendaciones = recomendacionesInput.getText().toString().trim();

        if (recomendaciones.isEmpty()) {
            Toast.makeText(this, "Las recomendaciones no pueden estar vacías", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> mesesFloracion = obtenerMesesSeleccionados();
        if (mesesFloracion.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar al menos un mes de floración", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            subirFoto(nombrePlanta, mesesFloracion, recomendaciones);
        } else {
            Toast.makeText(this, "Debes agregar una foto", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> obtenerMesesSeleccionados() {
        List<String> mesesSeleccionados = new ArrayList<>();
        if (checkBoxEnero.isChecked()) mesesSeleccionados.add("Enero");
        if (checkBoxFebrero.isChecked()) mesesSeleccionados.add("Febrero");
        if (checkBoxMarzo.isChecked()) mesesSeleccionados.add("Marzo");
        if (checkBoxAbril.isChecked()) mesesSeleccionados.add("Abril");
        if (checkBoxMayo.isChecked()) mesesSeleccionados.add("Mayo");
        if (checkBoxJunio.isChecked()) mesesSeleccionados.add("Junio");
        if (checkBoxJulio.isChecked()) mesesSeleccionados.add("Julio");
        if (checkBoxAgosto.isChecked()) mesesSeleccionados.add("Agosto");
        if (checkBoxSeptiembre.isChecked()) mesesSeleccionados.add("Septiembre");
        if (checkBoxOctubre.isChecked()) mesesSeleccionados.add("Octubre");
        if (checkBoxNoviembre.isChecked()) mesesSeleccionados.add("Noviembre");
        if (checkBoxDiciembre.isChecked()) mesesSeleccionados.add("Diciembre");
        return mesesSeleccionados;
    }

    private void subirFoto(String nombrePlanta, List<String> mesesFloracion, String recomendaciones) {
        // Referencia a la carpeta de almacenamiento en Firebase
        StorageReference storageRef = storage.getReference().child("plantas/" + nombrePlanta + ".jpg");

        // Subir la foto
        UploadTask uploadTask = storageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Obtener la URL de la foto
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                guardarNuevaPlanta(nombrePlanta, mesesFloracion, recomendaciones, uri.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
        });
    }

    private void guardarNuevaPlanta(String nombre, List<String> mesesFloracion, String recomendaciones, String fotoPlanta) {
        // Crear datos de la planta
        PlantData plantData = new PlantData(nombre, mesesFloracion, recomendaciones, fotoPlanta);

        // Guardar en Firestore
        db.collection("info_planta").document(nombre)
                .set(plantData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Planta guardada exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad actual y regresar a la anterior
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la planta", Toast.LENGTH_SHORT).show());
    }

    // Clase PlantData para representar la información de la planta
    public class PlantData {
        private String nombre;
        private List<String> mesesFloracion;
        private String recomendaciones;
        private String fotoPlanta; // Agregado para almacenar la URL de la foto

        public PlantData() {
            // Firestore requiere un constructor vacío
        }

        public PlantData(String nombre, List<String> mesesFloracion, String recomendaciones, String fotoPlanta) {
            this.nombre = nombre;
            this.mesesFloracion = mesesFloracion;
            this.recomendaciones = recomendaciones;
            this.fotoPlanta = fotoPlanta;
        }

        public String getNombre() {
            return nombre;
        }

        public List<String> getMesesFloracion() {
            return mesesFloracion;
        }

        public String getRecomendaciones() {
            return recomendaciones;
        }

        public String getFotoPlanta() {
            return fotoPlanta;
        }
    }
}
