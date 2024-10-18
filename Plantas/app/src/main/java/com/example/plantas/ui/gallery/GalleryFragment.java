package com.example.plantas.ui.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.plantas.R;
import com.example.plantas.databinding.FragmentGalleryBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private FirebaseFirestore db;
    private TableLayout tableLayout;
    private List<String> plantNames = new ArrayList<>();
    private static final String TAG = "GalleryFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializa Firestore y la tabla
        db = FirebaseFirestore.getInstance();
        tableLayout = binding.tableFloracion;  // Asegúrate de usar el binding correcto

        // Cargar plantas usando el UID del usuario
        loadRealPlants();

        return root;
    }

    private void loadRealPlants() {
        String userId = getCurrentUserId();  // Obtener el UID del usuario

        if (userId != null) {
            Log.d(TAG, "Cargando plantas del usuario: " + userId);

            // Obtener las plantas reales del perfil del usuario
            db.collection(userId)
                    .document("datos_perfil")  // Acceder directamente al documento 'datos_perfil'
                    .get()  // Obtener el documento específico
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "Documento obtenido: " + document); // Log del documento

                            if (document != null && document.exists()) {
                                Log.d(TAG, "El documento existe y contiene datos.");
                                // Obtener el mapa de plantas
                                Map<String, Object> plantasMap = (Map<String, Object>) document.get("Plantas_reales_usando");
                                if (plantasMap != null) {
                                    plantNames.addAll(plantasMap.keySet()); // Agregar los nombres de plantas al listado
                                    for (String plantName : plantNames) {
                                        Log.d(TAG, "Planta añadida: " + plantName);
                                    }
                                    // Cargar la cabecera después de obtener las plantas
                                    loadPlantTableHeader();
                                    // Cargar los meses de floración
                                    loadBloomingMonths();
                                } else {
                                    Log.d(TAG, "El mapa de plantas es nulo.");
                                }
                            } else {
                                Log.d(TAG, "No se encontró el documento de plantas reales.");
                            }
                        } else {
                            Log.e(TAG, "Error al obtener plantas reales.", task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "Error: UID es nulo, no se puede cargar las plantas.");
        }
    }

    private void loadPlantTableHeader() {
        Log.d(TAG, "Cargando cabeceras de la tabla con las plantas.");

        // Obtener la fila de cabecera (la primera fila)
        TableRow headerRow = (TableRow) tableLayout.getChildAt(0);

        // Añadir las plantas como cabecera
        for (String plantName : plantNames) {
            TextView plantHeader = new TextView(getContext());
            plantHeader.setText(plantName);
            plantHeader.setPadding(8, 8, 8, 8);
            plantHeader.setBackgroundColor(getResources().getColor(R.color.verde_lima_oscuro));
            plantHeader.setTextColor(getResources().getColor(R.color.white));
            headerRow.addView(plantHeader);
        }
    }

    private void loadBloomingMonths() {
        Log.d(TAG, "Cargando los meses de floración para las plantas.");

        for (String plantName : plantNames) {
            Log.d(TAG, "Cargando meses de floración para: " + plantName);
            db.collection("info_planta").document(plantName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d(TAG, "Documento de Info_planta obtenido: " + documentSnapshot); // Log del documento
                        if (documentSnapshot.exists()) {
                            List<String> bloomingMonths = (List<String>) documentSnapshot.get("mesesFloracion");
                            Log.d(TAG, "Meses de floración de " + plantName + ": " + bloomingMonths);
                            markBloomingMonths(plantName, bloomingMonths);
                        } else {
                            Log.d(TAG, "No se encontró información de floración para " + plantName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener meses de floración para " + plantName, e);
                    });
        }
    }

    private void markBloomingMonths(String plantName, List<String> bloomingMonths) {
        // Recorre cada fila correspondiente a un mes
        for (int rowIndex = 1; rowIndex < tableLayout.getChildCount(); rowIndex++) {  // Empieza desde 1 para saltar la cabecera
            TableRow row = (TableRow) tableLayout.getChildAt(rowIndex);
            TextView monthTextView = (TextView) row.getChildAt(0); // El TextView del mes está en la primera columna

            String monthName = monthTextView.getText().toString();
            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(8, 8, 8, 8);

            // Ajustar el tamaño del ImageView
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(50, 50);  // Ajustar el tamaño de los puntos
            imageView.setLayoutParams(layoutParams);

            // Si el mes está en la lista de meses de floración, se marca
            if (bloomingMonths != null && bloomingMonths.contains(monthName)) {
                imageView.setImageResource(R.drawable.circle_shape); // Cambia este recurso al que representa floración
            } else {
                imageView.setImageResource(R.drawable.circle_gray);  // Cambia este recurso al que representa "sin floración"
            }

            // Añadir la imagen en la columna correspondiente de la planta
            row.addView(imageView);
        }
    }

    private String getCurrentUserId() {
        // Obtener el UID desde SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        return sharedPreferences.getString("uid", null);  // Retorna el UID si está disponible
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
