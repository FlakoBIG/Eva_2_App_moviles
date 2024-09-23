package com.example.plantas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registrar extends AppCompatActivity {

    private Button registrarBoton;
    private EditText nombreInput, apellidoInput, correoInput, contraseniaInput;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Inicializa Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializa los componentes de la interfaz
        registrarBoton = findViewById(R.id.registrar_boton);
        nombreInput = findViewById(R.id.nombre_input);
        apellidoInput = findViewById(R.id.apellido_input);
        correoInput = findViewById(R.id.correo_input);
        contraseniaInput = findViewById(R.id.contrasenia_input);

        setup();
    }

    private void setup() {
        registrarBoton.setOnClickListener(v -> {
            // Verifica si los campos están llenos
            if (!correoInput.getText().toString().isEmpty() &&
                    !contraseniaInput.getText().toString().isEmpty() &&
                    !nombreInput.getText().toString().isEmpty() &&
                    !apellidoInput.getText().toString().isEmpty()) {

                String correo = correoInput.getText().toString();
                String contrasenia = contraseniaInput.getText().toString();
                String nombre = nombreInput.getText().toString();
                String apellido = apellidoInput.getText().toString();

                // Crear usuario en Firebase Authentication
                mAuth.createUserWithEmailAndPassword(correo, contrasenia)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Usuario registrado correctamente
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String uid = user.getUid();  // Obtener UID del usuario

                                    // Crear la colección en Firestore con el UID del usuario
                                    Map<String, Object> perfilData = new HashMap<>();
                                    perfilData.put("Nombre", nombre);
                                    perfilData.put("Apellido", apellido);
                                    perfilData.put("primera_vez", "si");

                                    db.collection(uid)
                                            .document("datos_perfil")
                                            .set(perfilData)
                                            .addOnSuccessListener(aVoid -> {
                                                // Registro y guardado de datos exitoso
                                                Toast.makeText(this, "Usuario Registrado con exito :D", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(this, Iniciar_sesion.class);
                                                startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> {
                                                // Error al crear la colección en Firestore
                                                Toast.makeText(this, "Error al guardar los datos en Firestore", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                // Error al registrar el usuario
                                Toast.makeText(this, "Algo pasó, no se pudo registrar", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Si algún campo está vacío
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para ir a la pantalla de inicio de sesión
    public void logearse(View v) {
        Intent intent = new Intent(this, Iniciar_sesion.class);
        startActivity(intent);
    }
}
