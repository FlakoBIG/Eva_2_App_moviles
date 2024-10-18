package com.example.plantas;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView; // Asegúrate de importar esto
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class Foto_planta_seleccionada extends AppCompatActivity {

    private ImageView imageView;
    private ImageButton btnFlechaIzquierda, btnFlechaDerecha;
    private TextView textoNombrePlanta; // TextView para mostrar el nombre de la planta
    private ArrayList<String> fotos; // Lista de fotos
    private int currentIndex; // Índice de la foto actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_planta_seleccionada);

        imageView = findViewById(R.id.img_cala);
        btnFlechaIzquierda = findViewById(R.id.btn_left);
        btnFlechaDerecha = findViewById(R.id.btn_right);
        textoNombrePlanta = findViewById(R.id.texto_nombre_planta); // Asegúrate de que este ID coincida

        // Obtener la lista de fotos y el índice actual
        fotos = getIntent().getStringArrayListExtra("fotos"); // Asegúrate de enviar la lista de fotos desde la actividad anterior
        currentIndex = getIntent().getIntExtra("current_index", 0); // Obtener el índice actual de la foto

        // Obtener el nombre de la planta y establecerlo en el TextView
        String nombrePlanta = getIntent().getStringExtra("nombre"); // Obtener el nombre de la planta
        textoNombrePlanta.setText(nombrePlanta); // Establecer el nombre de la planta

        // Cargar la foto inicial
        cargarFoto();

        btnFlechaIzquierda.setOnClickListener(v -> cambiarFoto(-1));
        btnFlechaDerecha.setOnClickListener(v -> cambiarFoto(1));
    }

    private void cargarFoto() {
        if (fotos != null && !fotos.isEmpty()) {
            String fotoUrl = fotos.get(currentIndex);
            Glide.with(this).load(fotoUrl).into(imageView);
        }
    }

    private void cambiarFoto(int direction) {
        currentIndex += direction;
        // Asegurarse de que el índice esté dentro de los límites
        if (currentIndex < 0) {
            currentIndex = fotos.size() - 1; // Volver al final de la lista si está en el inicio
        } else if (currentIndex >= fotos.size()) {
            currentIndex = 0; // Volver al inicio si está al final
        }
        cargarFoto(); // Cargar la nueva foto

        // Actualizar el nombre de la planta si es necesario
        // Aquí puedes agregar lógica para actualizar el nombre de la planta basado en el índice
    }
}
