package com.example.plantas;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlantResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_result);

        // Obtener el ImageView y TextView
        ImageView plantImageView = findViewById(R.id.plantImageView);
        TextView plantNameTextView = findViewById(R.id.plantNameTextView);

        // Obtener el Uri de la imagen y el nombre de la planta desde el Intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        String plantName = getIntent().getStringExtra("plantName");

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);

            // Establecer la imagen en el ImageView
            plantImageView.setImageURI(imageUri);
        }

        // Mostrar el nombre de la planta en el TextView
        plantNameTextView.setText(plantName);
    }
}
