package com.example.plantas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarTareaActivity extends AppCompatActivity {

    private EditText etNombreTarea;
    private Button btnGuardarTarea;
    private ListView listaTareas;
    private TaskAdapter taskAdapter; // Adaptador para mostrar la lista de tareas
    private List<Task> tareas; // Lista de tareas
    private String uid; // Variable para almacenar el UID
    private CheckBox cbEsRegar; // CheckBox para la opción de regar

    private FirebaseAuth auth; // FirebaseAuth para obtener el UID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarea);

        etNombreTarea = findViewById(R.id.et_tarea_nombre);
        btnGuardarTarea = findViewById(R.id.btn_guardar_tarea);
        listaTareas = findViewById(R.id.lista_tareas);
        cbEsRegar = findViewById(R.id.cb_es_regar); // Inicializar el CheckBox

        tareas = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, tareas);
        listaTareas.setAdapter(taskAdapter);

        auth = FirebaseAuth.getInstance(); // Inicializar FirebaseAuth
        uid = auth.getCurrentUser().getUid(); // Obtener el UID del usuario autenticado

        btnGuardarTarea.setOnClickListener(v -> {
            String nombreTarea = etNombreTarea.getText().toString();
            String esRegar = cbEsRegar.isChecked() ? "Sí" : "No"; // Obtener el estado del CheckBox

            if (!nombreTarea.isEmpty()) {
                guardarTareaEnFirebase(nombreTarea, esRegar);
            } else {
                Toast.makeText(AgregarTareaActivity.this, "Ingrese el nombre de la tarea", Toast.LENGTH_SHORT).show();
            }
        });

        cargarTareasDesdeFirebase();
    }

    private void guardarTareaEnFirebase(String nombreTarea, String esRegar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tarea = new HashMap<>();
        tarea.put("nombre", nombreTarea);
        tarea.put("regar", esRegar); // Guardar la información del CheckBox

        db.collection(uid).document("tareas").collection("mis_tareas")
                .add(tarea)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AgregarTareaActivity.this, "Tarea guardada", Toast.LENGTH_SHORT).show();
                    cargarTareasDesdeFirebase();
                    etNombreTarea.setText(""); // Limpiar el campo después de guardar
                    cbEsRegar.setChecked(false); // Reiniciar el CheckBox
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarTareaActivity.this, "Error al guardar tarea", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarTareasDesdeFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(uid).document("tareas").collection("mis_tareas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tareas.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String nombre = document.getString("nombre");
                        String esRegar = document.getString("regar");
                        tareas.add(new Task(id, nombre, esRegar));
                    }
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarTareaActivity.this, "Error al cargar tareas", Toast.LENGTH_SHORT).show();
                });
    }

    public void editarTarea(Task tarea) {
        etNombreTarea.setText(tarea.getNombre());
        cbEsRegar.setChecked(tarea.getRegar().equals("Sí")); // Marcar el CheckBox según el valor
        btnGuardarTarea.setText("Actualizar Tarea");

        btnGuardarTarea.setOnClickListener(v -> {
            String nuevoNombreTarea = etNombreTarea.getText().toString();
            String esRegar = cbEsRegar.isChecked() ? "Sí" : "No"; // Obtener el nuevo estado del CheckBox

            if (!nuevoNombreTarea.isEmpty()) {
                actualizarTareaEnFirebase(tarea.getId(), nuevoNombreTarea, esRegar);
            } else {
                Toast.makeText(AgregarTareaActivity.this, "Ingrese un nuevo nombre para la tarea", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarTareaEnFirebase(String tareaId, String nuevoNombre, String esRegar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tareaActualizada = new HashMap<>();
        tareaActualizada.put("nombre", nuevoNombre);
        tareaActualizada.put("regar", esRegar); // Actualizar el estado del CheckBox

        db.collection(uid).document("tareas").collection("mis_tareas").document(tareaId)
                .update(tareaActualizada)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AgregarTareaActivity.this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
                    cargarTareasDesdeFirebase();
                    btnGuardarTarea.setText("Guardar Tarea");
                    etNombreTarea.setText(""); // Limpiar el campo después de actualizar
                    cbEsRegar.setChecked(false); // Reiniciar el CheckBox
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarTareaActivity.this, "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                });
    }

    public void eliminarTarea(Task tarea) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tarea")
                .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarTareaDeFirebase(tarea.getId()))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarTareaDeFirebase(String tareaId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(uid).document("tareas").collection("mis_tareas").document(tareaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AgregarTareaActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    cargarTareasDesdeFirebase();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarTareaActivity.this, "Error al eliminar tarea", Toast.LENGTH_SHORT).show();
                });
    }
}
