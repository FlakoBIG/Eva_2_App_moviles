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

        // inicializa firestore y la tabla
        db = FirebaseFirestore.getInstance();
        tableLayout = binding.tableFloracion;  // asegurate de usar el binding correcto

        // cargar plantas usando el uid del usuario
        loadRealPlants();

        return root;
    }

    private void loadRealPlants() {
        String userId = getCurrentUserId();  // obtener el uid del usuario

        if (userId != null) {
            Log.d(TAG, "cargando plantas del usuario: " + userId);

            // obtener las plantas reales del perfil del usuario
            db.collection(userId)
                    .document("datos_perfil")  // acceder directamente al documento 'datos_perfil'
                    .get()  // obtener el documento específico
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "documento obtenido: " + document); // log del documento

                            if (document != null && document.exists()) {
                                Log.d(TAG, "el documento existe y tiene datos.");
                                // obtener el mapa de plantas
                                Map<String, Object> plantasMap = (Map<String, Object>) document.get("Plantas_reales_usando");
                                if (plantasMap != null) {
                                    plantNames.addAll(plantasMap.keySet()); // agregar los nombres de plantas al listado
                                    for (String plantName : plantNames) {
                                        Log.d(TAG, "planta añadida: " + plantName);
                                    }
                                    // cargar la cabecera después de obtener las plantas
                                    loadPlantTableHeader();
                                    // cargar los meses de floracion
                                    loadBloomingMonths();
                                } else {
                                    Log.d(TAG, "el mapa de plantas es nulo.");
                                }
                            } else {
                                Log.d(TAG, "no se encontro el documento de plantas reales.");
                            }
                        } else {
                            Log.e(TAG, "error al obtener plantas reales.", task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "error: uid es nulo, no se puede cargar las plantas.");
        }
    }

    private void loadPlantTableHeader() {
        Log.d(TAG, "cargando cabeceras de la tabla con las plantas.");

        // obtener la fila de cabecera (la primera fila)
        TableRow headerRow = (TableRow) tableLayout.getChildAt(0);

        // añadir las plantas como cabecera
        for (String plantName : plantNames) {
            TextView plantHeader = new TextView(getContext());
            plantHeader.setText(plantName);
            plantHeader.setPadding(8, 8, 8, 8);
            plantHeader.setBackgroundColor(getResources().getColor(R.color.verde_lima_oscuro));
            plantHeader.setTextColor(getResources().getColor(R.color.white));

            // no dejar que se extienda la cabecera por toda la pantalla
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            plantHeader.setLayoutParams(layoutParams);

            headerRow.addView(plantHeader);
        }
    }

    private void loadBloomingMonths() {
        Log.d(TAG, "cargando los meses de floracion para las plantas.");

        for (String plantName : plantNames) {
            Log.d(TAG, "cargando meses de floracion para: " + plantName);
            db.collection("info_planta").document(plantName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d(TAG, "documento de info_planta obtenido: " + documentSnapshot); // log del documento
                        if (documentSnapshot.exists()) {
                            List<String> bloomingMonths = (List<String>) documentSnapshot.get("mesesFloracion");
                            Log.d(TAG, "meses de floracion de " + plantName + ": " + bloomingMonths);
                            markBloomingMonths(plantName, bloomingMonths);
                        } else {
                            Log.d(TAG, "no se encontro informacion de floracion para " + plantName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "error al obtener meses de floracion para " + plantName, e);
                    });
        }
    }

    private void markBloomingMonths(String plantName, List<String> bloomingMonths) {
        // recorre cada fila correspondiente a un mes
        for (int rowIndex = 1; rowIndex < tableLayout.getChildCount(); rowIndex++) {  // empieza desde 1 para saltar la cabecera
            TableRow row = (TableRow) tableLayout.getChildAt(rowIndex);
            TextView monthTextView = (TextView) row.getChildAt(0); // el TextView del mes está en la primera columna

            String monthName = monthTextView.getText().toString();
            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(8, 8, 8, 8);

            // ajustar el tamaño del ImageView
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(50, 50);  // ajustar el tamaño de los puntos
            imageView.setLayoutParams(layoutParams);

            // si el mes está en la lista de meses de floracion, se marca
            if (bloomingMonths != null && bloomingMonths.contains(monthName)) {
                imageView.setImageResource(R.drawable.circle_shape); // cambia este recurso al que representa floracion
            } else {
                imageView.setImageResource(R.drawable.circle_gray);  // cambia este recurso al que representa "sin floracion"
            }

            // añadir la imagen en la columna correspondiente de la planta
            row.addView(imageView);
        }
    }

    private String getCurrentUserId() {
        // obtener el uid desde SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        return sharedPreferences.getString("uid", null);  // retorna el uid si está disponible
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
