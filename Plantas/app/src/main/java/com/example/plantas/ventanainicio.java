package com.example.plantas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plantas.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ventanainicio extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Uri photoUri;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    // guardamos el navcontroller aca
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hacer el binding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> {
            // revisa si el permiso pa la camara ta concedido
            if (ContextCompat.checkSelfPermission(ventanainicio.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // si no hay permiso, lo pedimos
                ActivityCompat.requestPermissions(ventanainicio.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                // si ya tenemos permiso, abrimos la camara
                openCamera();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.action_cuenta)
                .setOpenableLayout(drawer)
                .build();

        // inicializamos navcontroller
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // actualizar nombre del header
        actualizarNombreEnHeader(navigationView);

        // inicializar el launcher de la camara
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        identificarPlanta(photoUri);  // uri ya ta almacenada en openCamera
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.misplantas) {
            // abrir la actividad de "mis plantas" con un intent
            Intent intent = new Intent(this, mis_plantas.class);
            startActivity(intent);
            return true; // devolvemos true si se manejo el item del menu
        }

        // comportamiento normal pa otros item del menu
        return NavigationUI.onNavDestinationSelected(item, navController);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cuenta) {
            Log.d("MenuDebug", "boton cuenta seleccionado");
            Intent intent = new Intent(this, cuenta.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void actualizarNombreEnHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView nombreTextView = headerView.findViewById(R.id.textView);

        SharedPreferences sharedPreferences = getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
        String nombre = sharedPreferences.getString("nombre", "nombre no encontrado");
        String apellido = sharedPreferences.getString("apellido", "apellido no encontrado");

        String nombreCompleto = nombre + " " + apellido;
        nombreTextView.setText(nombreCompleto);
    }

    // abirr  la camara
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // si hay error, lo mostramos
                Log.e("CameraError", "error creando el archivo de imagen", ex);
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.plantas.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                activityResultLauncher.launch(takePictureIntent); // lanzamos la actividad usando el launcher moderno
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // identificar la planta y pasar los datos a la nueva actividad
    private void identificarPlanta(Uri imageUri) {
        if (imageUri != null) {
            // simulamos el nombre de la planta por ahora
            String nombrePlanta = "planta desconocida";

            // abrimos la nueva actividad y pasamos los datos
            Intent intent = new Intent(this, PlantResultActivity.class);
            intent.putExtra("imageUri", imageUri.toString());  // pasamos el uri de la imagen
            intent.putExtra("plantName", nombrePlanta);  // pasamos el nombre de la planta
            startActivity(intent);
        } else {
            Toast.makeText(this, "error al identificar la planta", Toast.LENGTH_SHORT).show();
        }
    }

    // manejar resultados de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permiso concedido, abrimos la camara
                openCamera();
            } else {
                // permiso denegado, mostramos mensaje
                Toast.makeText(this, "permiso de camara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
