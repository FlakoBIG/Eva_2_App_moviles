package com.example.plantas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class View_plantita extends AppCompatActivity {

    private Button btnAgregarEntrada;
    private LinearLayout ventanitaAgregarDiario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_plantita);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnGuia = findViewById(R.id.btn_guia);
        btnGuia.setOnClickListener(v -> {
            Intent intent = new Intent(View_plantita.this, Giua_planta.class);
            startActivity(intent);
        });

        Button btnFotos = findViewById(R.id.btn_fotos);
        btnFotos.setOnClickListener(v -> {
            Intent intent = new Intent(View_plantita.this, Fotos_planta.class);
            startActivity(intent);
        });

        Button btnEliminarPlanta = findViewById(R.id.btn_eliminar_planta);
        btnEliminarPlanta.setOnClickListener(v -> {
            new AlertDialog.Builder(View_plantita.this)
                    .setTitle("Eliminar Planta")
                    .setMessage("¿Estás seguro que quieres eliminar esta planta?")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            eliminarPlanta();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

    }

    private void eliminarPlanta() {
        Intent intent = new Intent(View_plantita.this, mis_plantas.class);
        startActivity(intent);
        finish();
    }
}
