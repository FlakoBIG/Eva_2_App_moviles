package com.example.plantas.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.firebase.firestore.DocumentSnapshot;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.plantas.R;
import com.example.plantas.AgregarTareaActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private ImageView imagenPlanta;
    private TextView textoCantidadPlantasValor;
    private TextView textoPlantasMuertasValor;
    private TextView textoUsoAguaValor;
    private TextView textoPlantaMasAntiguaValor;
    private TableLayout tablaTareas;
    private Button btnAgregarTarea;

    private FirebaseAuth autenticacion;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        imagenPlanta = vista.findViewById(R.id.plant_image);
        textoCantidadPlantasValor = vista.findViewById(R.id.text_plant_quantity_value);
        textoPlantasMuertasValor = vista.findViewById(R.id.text_dead_plants_value);
        textoUsoAguaValor = vista.findViewById(R.id.text_water_usage_value);
        textoPlantaMasAntiguaValor = vista.findViewById(R.id.text_oldest_plant_value);
        tablaTareas = vista.findViewById(R.id.task_table);
        btnAgregarTarea = vista.findViewById(R.id.btn_add_task);
        Button btnActualizar = vista.findViewById(R.id.btn_refresh); // Inicializar el botón de actualización

        autenticacion = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Configurar listener para agregar tarea
        btnAgregarTarea.setOnClickListener(v -> {
            // Iniciar la actividad para agregar tarea
            Intent intent = new Intent(getActivity(), AgregarTareaActivity.class);
            startActivity(intent);
        });

        // Configurar listener para el botón de actualización
        btnActualizar.setOnClickListener(v -> {
            cargarDatosUsuario(); // Llamar a cargarDatosUsuario
            cargarTareas(autenticacion.getCurrentUser().getUid()); // Llamar a cargarTareas
        });

        // Cargar tareas desde Firestore
        cargarTareas(autenticacion.getCurrentUser().getUid());

        return vista;
    }

    private void cargarDatosUsuario() {
        if (autenticacion.getCurrentUser() != null) {
            String uid = autenticacion.getCurrentUser().getUid();
            firestore.collection(uid).document("datos_jardin").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String urlImagen = documentSnapshot.getString("Foto");
                            long cantidadPlantas = documentSnapshot.getLong("cantidad_de_plantas");
                            long cantidadPlantasMuertas = documentSnapshot.getLong("cantidad_de_plantas_perecidas");
                            long litrosDeAguaGastados = documentSnapshot.getLong("cantidad_litros_agua_gastados_mes");
                            String plantaMasAntigua = documentSnapshot.getString("planta_mas_antigua");

                            textoCantidadPlantasValor.setText(String.valueOf(cantidadPlantas));
                            textoCantidadPlantasValor.setTextColor(ContextCompat.getColor(getContext(), R.color.verde_lima));

                            textoPlantasMuertasValor.setText(String.valueOf(cantidadPlantasMuertas));
                            textoPlantasMuertasValor.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));

                            textoUsoAguaValor.setText(litrosDeAguaGastados + " Litros");
                            textoUsoAguaValor.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark));

                            textoPlantaMasAntiguaValor.setText(plantaMasAntigua);
                            textoPlantaMasAntiguaValor.setTextColor(ContextCompat.getColor(getContext(), R.color.verde_lima));

                            new TareaDescargarImagen(imagenPlanta).execute(urlImagen);
                        }
                    }).addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }

    private void agregarTarea(String descripcionTarea, boolean completada, String idTarea) {
        TableRow filaTabla = new TableRow(getActivity());
        filaTabla.setPadding(16, 16, 16, 16); // Padding en la fila
        filaTabla.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)); // Alineación completa

        // LinearLayout para alinear el icono y el texto
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        ImageView iconoTarea = new ImageView(getActivity());
        iconoTarea.setImageResource(R.drawable.circle_shape);
        iconoTarea.setLayoutParams(new LinearLayout.LayoutParams(60, 45));
        iconoTarea.setPadding(8, 0, 8, 0); // Padding para el icono

        TextView textoTarea = new TextView(getActivity());
        textoTarea.setText(descripcionTarea);
        textoTarea.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        textoTarea.setTextSize(18); // Aumentar el tamaño de la letra
        textoTarea.setPadding(8, 0, 0, 0); // Padding para el texto

        linearLayout.addView(iconoTarea);
        linearLayout.addView(textoTarea);
        filaTabla.addView(linearLayout);

        Button botonCompletado = new Button(getActivity());
        botonCompletado.setText("✓");
        botonCompletado.setOnClickListener(v -> completarTarea(idTarea, descripcionTarea));

        filaTabla.addView(botonCompletado);
        tablaTareas.addView(filaTabla);
    }

    private void completarTarea(String idTarea, String descripcionTarea) {
        String uid = autenticacion.getCurrentUser().getUid();

        // Mostrar un diálogo de confirmación antes de completar la tarea
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirmar tarea")
                .setMessage("¿Completaste la tarea " + descripcionTarea + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Obtener la tarea específica para verificar el campo "regar"
                    firestore.collection(uid).document("tareas").collection("mis_tareas").document(idTarea)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String regar = documentSnapshot.getString("regar");
                                    if ("sí".equalsIgnoreCase(regar)) {
                                        // Actualizar cantidad de litros si la tarea es "regar"
                                        firestore.collection(uid).document("datos_jardin")
                                                .update("cantidad_litros_agua_gastados_mes", FieldValue.increment(1))
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getActivity(), "Consumo de agua actualizado.", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    e.printStackTrace();
                                                    Toast.makeText(getActivity(), "Error al actualizar el consumo de agua", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                                // Eliminar la tarea
                                firestore.collection(uid).document("tareas").collection("mis_tareas").document(idTarea)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getActivity(), "Tarea completada", Toast.LENGTH_SHORT).show();
                                            cargarTareas(uid); // Recargar tareas después de eliminar
                                        })
                                        .addOnFailureListener(e -> {
                                            e.printStackTrace();
                                            Toast.makeText(getActivity(), "Error al eliminar la tarea", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Error al obtener los detalles de la tarea", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // No hacer nada si se selecciona "No"
                    Toast.makeText(getActivity(), "Tarea no completada", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void cargarTareas(String uid) {
        firestore.collection(uid).document("tareas").collection("mis_tareas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tablaTareas.removeAllViews(); // Limpiar la tabla antes de cargar tareas

                    // Verificar si hay tareas
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Crear un TextView para mostrar el mensaje "No hay tareas"
                        TextView noTareasTextView = new TextView(getActivity());
                        noTareasTextView.setText("No hay tareas");
                        noTareasTextView.setTextSize(18);
                        noTareasTextView.setPadding(16, 16, 16, 16);
                        noTareasTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                        noTareasTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

                        // Agregar el mensaje a la tabla
                        tablaTareas.addView(noTareasTextView);
                    } else {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String descripcionTarea = document.getString("nombre");
                            Boolean completada = document.getBoolean("completado");
                            String idTarea = document.getId(); // Obtener el ID del documento
                            agregarTarea(descripcionTarea, completada != null && completada, idTarea);
                        }
                    }
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }


    private static class TareaDescargarImagen extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public TareaDescargarImagen(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlMostrar = urls[0];
            Bitmap imagen = null;
            try {
                URL url = new URL(urlMostrar);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoInput(true);
                conexion.connect();
                InputStream input = conexion.getInputStream();
                imagen = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imagen;
        }

        protected void onPostExecute(Bitmap resultado) {
            imageView.setImageBitmap(resultado);
        }
    }
}
