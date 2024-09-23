package com.example.plantas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class cuenta extends AppCompatActivity {

    private TextView correoView, contraseniaView;
    private Button cerrarSesionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        // conectar ui a variables
        correoView = findViewById(R.id.correo_view);
        contraseniaView = findViewById(R.id.contrasenia_view);
        cerrarSesionBtn = findViewById(R.id.cerrar_sesion_btn);

        // mostrar los datos de sharedpreferences
        mostrarCredenciales();

        // listener para cerrar sesion
        cerrarSesionBtn.setOnClickListener(v -> cerrarSesion());
    }

    private void mostrarCredenciales() {
        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        String correo = sharedPreferences.getString("correo", "correo no disponible");
        String contrasenia = sharedPreferences.getString("contrasenia", "contrasenia no disponible");

        // mostrar el correo y la contrasenia en los textviews
        correoView.setText(correo);
        contraseniaView.setText(contrasenia);
    }

    private void cerrarSesion() {
        // limpiar sharedpreferences para eliminar las credenciales guardadas
        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // limpiar todos los datos guardados
        editor.apply();

        // redirigir a la pantalla de inicio de sesion
        Intent intent = new Intent(this, Iniciar_sesion.class);
        startActivity(intent);
        finish();  // finaliza la actividad actual para que no pueda volver
    }
}
