package com.example.plantas.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.plantas.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private ImageView plantImage;
    private TextView textPlantQuantityValue;
    private TextView textDeadPlantsValue;
    private TextView textWaterUsageValue;
    private TextView textOldestPlantValue;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        plantImage = view.findViewById(R.id.plant_image);
        textPlantQuantityValue = view.findViewById(R.id.text_plant_quantity_value);
        textDeadPlantsValue = view.findViewById(R.id.text_dead_plants_value);
        textWaterUsageValue = view.findViewById(R.id.text_water_usage_value);
        textOldestPlantValue = view.findViewById(R.id.text_oldest_plant_value);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

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
                            //para agregarle culur
                            textPlantQuantityValue.setTextColor(ContextCompat.getColor(getContext(), R.color.verde_lima));

                            textDeadPlantsValue.setText(String.valueOf(cantidadPlantasPerecidas));
                            //para agregarle culur
                            textDeadPlantsValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));

                            textWaterUsageValue.setText(litrosDeAguaGastados + " Litros");
                            //para agregarle culur
                            textWaterUsageValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark));

                            textOldestPlantValue.setText(plantaMasAntigua);
                            //para agregarle colorsh
                            textOldestPlantValue.setTextColor(ContextCompat.getColor(getContext(), R.color.verde_lima));

                            new DownloadImageTask(plantImage).execute(imageUrl);
                        }
                    }).addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }

        return view;
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
