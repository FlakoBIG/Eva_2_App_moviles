package com.example.plantas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Registrar extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private Button registrarBoton, googleButton, facebookButton;
    private EditText nombreInput, apellidoInput, correoInput, contraseniaInput;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Inicializa Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializa los componentes de la interfaz
        registrarBoton = findViewById(R.id.registrar_boton);
        googleButton = findViewById(R.id.Google_btn);
        facebookButton = findViewById(R.id.Facebook);
        nombreInput = findViewById(R.id.nombre_input);
        apellidoInput = findViewById(R.id.apellido_input);
        correoInput = findViewById(R.id.correo_input);
        contraseniaInput = findViewById(R.id.contrasenia_input);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configura Facebook Login
        callbackManager = CallbackManager.Factory.create();

        setup();
    }

    private void setup() {
        // Registro normal con correo y contraseña
        registrarBoton.setOnClickListener(v -> {
            if (!correoInput.getText().toString().isEmpty() &&
                    !contraseniaInput.getText().toString().isEmpty() &&
                    !nombreInput.getText().toString().isEmpty() &&
                    !apellidoInput.getText().toString().isEmpty()) {

                String correo = correoInput.getText().toString();
                String contrasenia = contraseniaInput.getText().toString();
                String nombre = nombreInput.getText().toString();
                String apellido = apellidoInput.getText().toString();

                mAuth.createUserWithEmailAndPassword(correo, contrasenia)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String uid = user.getUid();
                                    Map<String, Object> perfilData = new HashMap<>();
                                    perfilData.put("Nombre", nombre);
                                    perfilData.put("Apellido", apellido);
                                    perfilData.put("primera_vez", "si");

                                    db.collection(uid).document("datos_perfil")
                                            .set(perfilData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Usuario Registrado con éxito :D", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, Iniciar_sesion.class));
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                Toast.makeText(this, "Algo pasó, no se pudo registrar", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // Login con Google
        googleButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Login con Facebook
        facebookButton.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(Registrar.this, "Login cancelado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(Registrar.this, "Error en Facebook Login", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Maneja el resultado de Google Sign-In
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult();
                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                Toast.makeText(this, "Error en Google Sign-In", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login con Google exitoso", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Iniciar_sesion.class));
            } else {
                Toast.makeText(this, "Error en Google Sign-In", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login con Facebook exitoso", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Iniciar_sesion.class));
            } else {
                Toast.makeText(this, "Error en Facebook Login", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
