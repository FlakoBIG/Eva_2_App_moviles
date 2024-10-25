package com.example.plantas;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class Alarma_noti extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TimePicker timePicker;
    private TextView tvHoraIngresada;
    private static final String PREFS_NAME = "AlarmaPrefs";
    private static final String HORA_KEY = "hora";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crearCanalNotificaciones();
        setContentView(R.layout.activity_alarma_noti);

        solicitarPermisoNotificaciones();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        timePicker = findViewById(R.id.timePicker);
        tvHoraIngresada = findViewById(R.id.tv_hora_ingresada);

        cargarHoraGuardada();

        findViewById(R.id.btn_programar_alarma).setOnClickListener(v -> programarAlarma());
        findViewById(R.id.btn_noti_ejemplo).setOnClickListener(v -> mostrarNotificacionEjemplo());
        findViewById(R.id.btn_clima).setOnClickListener(v -> obtenerClimaActual());
    }

    private void programarAlarma() {
        int hora = timePicker.getCurrentHour();
        int minuto = timePicker.getCurrentMinute();
        guardarHora(hora, minuto);

        Intent intent = new Intent(this, ClimaNotificacionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        // Programar la alarma para todos los días a la hora seleccionada
        // No importa si la hora ya pasó hoy, se activará mañana a la misma hora
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            // Si ya pasó la hora de hoy, programamos para mañana
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Configurar la alarma
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );

            long minutosRestantes = (calendar.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60);
            Toast.makeText(this, "Alarma programada para las " + hora + ":" + String.format("%02d", minuto) +
                    ". Faltan " + minutosRestantes + " minutos.", Toast.LENGTH_SHORT).show();
        }
    }



    private void guardarHora(int hora, int minuto) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HORA_KEY + "_hora", hora);
        editor.putInt(HORA_KEY + "_minuto", minuto);
        editor.apply();

        tvHoraIngresada.setText("Hora ingresada: " + hora + ":" + String.format("%02d", minuto));
    }

    private void cargarHoraGuardada() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int hora = sharedPreferences.getInt(HORA_KEY + "_hora", -1);
        int minuto = sharedPreferences.getInt(HORA_KEY + "_minuto", -1);

        if (hora != -1 && minuto != -1) {
            tvHoraIngresada.setText("Hora ingresada: " + hora + ":" + String.format("%02d", minuto));
        } else {
            tvHoraIngresada.setText("Hora ingresada: No programada");
        }
    }

    private void obtenerClimaActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            obtenerTemperatura(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void obtenerTemperatura(double lat, double lon) {
        new Thread(() -> {
            try {
                String apiKey = "4660ca2db86863b9d3c403a228826bec";
                String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                        "&lon=" + lon + "&units=metric&appid=" + apiKey;
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                WeatherResponse weatherResponse = new Gson().fromJson(response.toString(), WeatherResponse.class);
                if (weatherResponse != null) {
                    enviarNotificacionClima(weatherResponse.main.temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void enviarNotificacionClima(double temperatura) {
        String mensaje = "La temperatura es de " + temperatura + "°C.";
        if (temperatura > 30) {
            mensaje += " Mantén tus plantas en la sombra.";
        } else if (temperatura < 15) {
            mensaje += " Abriga tus plantitas.";
        } else {
            mensaje += " Es un día muy lindo.";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "clima_channel")
                .setSmallIcon(R.drawable.grain_png)
                .setContentTitle("Recomendación del clima")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(2, builder.build());
        }
    }

    private void mostrarNotificacionEjemplo() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ejemplo_channel")
                .setSmallIcon(R.drawable.grain_png)
                .setContentTitle("Notificación de Ejemplo")
                .setContentText("Esta es una notificación de prueba.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canalClima = new NotificationChannel(
                    "clima_channel",
                    "Canal Clima",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalClima.setDescription("Notificaciones del clima");
            canalClima.setLightColor(Color.BLUE);
            canalClima.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canalClima);
            }

            NotificationChannel canalEjemplo = new NotificationChannel(
                    "ejemplo_channel",
                    "Canal Ejemplo",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalEjemplo.setDescription("Notificaciones de ejemplo");
            canalEjemplo.setLightColor(Color.BLUE);
            canalEjemplo.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            if (manager != null) {
                manager.createNotificationChannel(canalEjemplo);
            }
        }
    }

    private class WeatherResponse {
        Main main;

        private class Main {
            double temp;
        }
    }
}
