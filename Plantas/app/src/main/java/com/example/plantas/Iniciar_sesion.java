package com.example.plantas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Iniciar_sesion extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText correoInput, contraseniaInput;
    private Button iniciarSesionBoton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // desactivar modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniciar_sesion);

        // inicializar firebase auth
        mAuth = FirebaseAuth.getInstance();

        // conectar ui a variables
        correoInput = findViewById(R.id.correo_view);
        contraseniaInput = findViewById(R.id.contrasenia_view);
        iniciarSesionBoton = findViewById(R.id.registrar_boton);

        // verificar si ya hay credenciales guardadas
        verificarCredencialesGuardadas();

        // establecer el listener para el boton de iniciar sesion
        iniciarSesionBoton.setOnClickListener(this::iniciarSesion);
    }

    private void verificarCredencialesGuardadas() {
        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        String correoGuardado = sharedPreferences.getString("correo", null);
        String contraseniaGuardada = sharedPreferences.getString("contrasenia", null);

        if (correoGuardado != null && contraseniaGuardada != null) {
            // si hay credenciales guardadas, intentar iniciar sesion automaticamente
            iniciarSesionAutomatico(correoGuardado, contraseniaGuardada);
        }
    }

    private void iniciarSesionAutomatico(String correo, String contrasenia) {
        mAuth.signInWithEmailAndPassword(correo, contrasenia)
                .addOnCompleteListener(this, task -> {
                    Toast.makeText(this, "cargando perfil", Toast.LENGTH_SHORT).show();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            guardarUID(uid);
                            obtenerDatosDeFirestore(uid, new FirestoreCallback() {
                                @Override
                                public void onCallback(boolean success, String primeraVez) {
                                    if (success) {
                                        // validar el campo "primera_vez"
                                        if ("si".equals(primeraVez)) {
                                            Intent intent = new Intent(Iniciar_sesion.this, CrearJardinActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Intent intent = new Intent(Iniciar_sesion.this, ventanainicio.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(Iniciar_sesion.this, "error al obtener los datos del perfil", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(this, "usuario no encontrado. intenta iniciar sesion de nuevo.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "error al iniciar sesion automaticamente", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void iniciarSesion(View v) {
        String correo = correoInput.getText().toString();
        String contrasenia = contraseniaInput.getText().toString();

        if (!correo.isEmpty() && !contrasenia.isEmpty()) {
            mAuth.signInWithEmailAndPassword(correo, contrasenia)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                guardarUID(uid);
                                guardarCredenciales(correo, contrasenia);

                                // obtener datos del perfil de firestore y luego redirigir
                                obtenerDatosDeFirestore(uid, new FirestoreCallback() {
                                    @Override
                                    public void onCallback(boolean success, String primeraVez) {
                                        if (success) {
                                            // validar el campo "primera_vez"
                                            if ("si".equals(primeraVez)) {
                                                Intent intent = new Intent(Iniciar_sesion.this, CrearJardinActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(Iniciar_sesion.this, ventanainicio.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } else {
                                            Toast.makeText(Iniciar_sesion.this, "error al obtener los datos del perfil", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(this, "error con tus datos", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "completa todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    // guardar uid en sharedpreferences
    private void guardarUID(String uid) {
        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid", uid);  // guardar el uid
        editor.apply();
    }

    // guardar correo y contrasenia en sharedpreferences
    private void guardarCredenciales(String correo, String contrasenia) {
        SharedPreferences sharedPreferences = getSharedPreferences("Credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("correo", correo);
        editor.putString("contrasenia", contrasenia);
        editor.apply();  // guarda los cambios
    }

    // obtener los datos de firestore con el uid
    private void obtenerDatosDeFirestore(String uid, FirestoreCallback firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(uid).document("datos_perfil")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("Nombre");
                        String apellido = documentSnapshot.getString("Apellido");
                        String primeraVez = documentSnapshot.getString("primera_vez"); // obtener el campo "primera_vez"

                        guardarDatosEnSharedPreferences(nombre, apellido);

                        // llamar el callback con el valor de primera_vez
                        firestoreCallback.onCallback(true, primeraVez);
                    } else {
                        firestoreCallback.onCallback(false, null);
                    }
                })
                .addOnFailureListener(e -> {
                    firestoreCallback.onCallback(false, null);
                });
    }

    // guardar datos del perfil en sharedpreferences
    private void guardarDatosEnSharedPreferences(String nombre, String apellido) {
        SharedPreferences sharedPreferences = getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nombre", nombre);
        editor.putString("apellido", apellido);
        editor.apply();
    }

    public void registrar(View v) {
        // redirigir a la pantalla de registro
        Intent intent = new Intent(this, Registrar.class);
        startActivity(intent);
    }

    // interfaz para el callback de firestore
    private interface FirestoreCallback {
        void onCallback(boolean success, String primeraVez);
    }

    public void mensaje(View v) {
        Toast.makeText(this, "hola kiunda", Toast.LENGTH_SHORT).show();
    }
}
