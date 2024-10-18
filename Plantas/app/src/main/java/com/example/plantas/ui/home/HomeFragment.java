package com.example.plantas.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.plantas.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private ImageView plantImage;
    private TextView textPlantQuantityValue;
    private TextView textDeadPlantsValue;
    private TextView textWaterUsageValue;
    private TextView textOldestPlantValue;
    private TableLayout taskTable;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        plantImage = view.findViewById(R.id.plant_image);
        textPlantQuantityValue = view.findViewById(R.id.text_plant_quantity_value);
        textDeadPlantsValue = view.findViewById(R.id.text_dead_plants_value);
        textWaterUsageValue = view.findViewById(R.id.text_water_usage_value);
        textOldestPlantValue = view.findViewById(R.id.text_oldest_plant_value);
        taskTable = view.findViewById(R.id.task_table);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserData();
        loadTasks();

        return view;
    }

    private void loadUserData() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            firestore.collection(uid).document("datos_jardin").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("Foto");
                            long cantidadPlantas = documentSnapshot.getLong("cantidad_de_plantas");
                            long cantidadPlantasPerecidas = documentSnapshot.getLong("cantidad_de_plantas_perecidas");
                            long litrosDeAguaGastados = documentSnapshot.getLong("cantidad_litros_agua_gastados_mes");
                            String plantaMasAntigua = documentSnapshot.getString("planta_mas_antigua");

                            textPlantQuantityValue.setText(String.valueOf(cantidadPlantas));
                            textPlantQuantityValue.setTextColor(ContextCompat.getColor(getContext(), R.color.verde_lima));

                            textDeadPlantsValue.setText(String.valueOf(cantidadPlantasPerecidas));
                            textDeadPlantsValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));

                            textWaterUsageValue.setText(litrosDeAguaGastados + " Litros");
                            textWaterUsageValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark));

                            textOldestPlantValue.setText(plantaMasAntigua);
                            textOldestPlantValue.setTextColor(ContextCompat.getColor(getContext(), R.color.verde_lima));

                            new DownloadImageTask(plantImage).execute(imageUrl);
                        }
                    }).addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }

    private void loadTasks() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            firestore.collection(uid).document("tareas").collection("mis_tareas")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Iterar sobre las tareas y crear filas en la tabla
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String taskDescription = document.getString("descripcion");
                            createTaskRow(taskDescription, document.getId());
                        }
                    }).addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }

    private void createTaskRow(String taskDescription, String taskId) {
        TableRow tableRow = new TableRow(getContext());

        // Crear TextView para la descripción de la tarea
        TextView taskTextView = new TextView(getContext());
        taskTextView.setText(taskDescription);
        taskTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

        // Crear botón para completar la tarea
        Button completeButton = new Button(getContext());
        completeButton.setText("Completar");
        completeButton.setOnClickListener(v -> completeTask(taskId, tableRow));

        // Agregar vistas a la fila
        tableRow.addView(taskTextView);
        tableRow.addView(completeButton);

        // Agregar la fila a la tabla
        taskTable.addView(tableRow);
    }

    private void completeTask(String taskId, TableRow tableRow) {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            firestore.collection(uid).document("tareas").collection("mis_tareas").document(taskId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        taskTable.removeView(tableRow);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bmp = null;
            try {
                URL url = new URL(urlDisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bmp = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
