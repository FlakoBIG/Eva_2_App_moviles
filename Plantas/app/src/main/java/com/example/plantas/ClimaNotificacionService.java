package com.example.plantas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ClimaNotificacionService extends Service {
    private static final String CHANNEL_ID = "ClimaNotificacion";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        crearCanalNotificacion(); // Crear el canal de notificación
        ClimaApi climaApi = new ClimaApi(this);
        climaApi.obtenerClima("Copiapo", new ClimaApi.ClimaListener() {
            @Override
            public void onClimaRecibido(double temperatura, int humedad) {
                String mensaje = generarMensajeClima(temperatura);
                enviarNotificacion(mensaje);
            }

            @Override
            public void onError(String mensaje) {
                Log.e("ClimaNotificacionService", mensaje);
            }
        });

        return START_NOT_STICKY;
    }

    private void crearCanalNotificacion() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notificaciones de Clima", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
    }

    private String generarMensajeClima(double temperatura) {
        String mensaje;
        if (temperatura > 30) {
            mensaje = "La temperatura es de " + temperatura + "°C. Mantén tus plantas en la sombra.";
        } else if (temperatura < 15) {
            mensaje = "La temperatura es de " + temperatura + "°C. Abriga tus plantitas.";
        } else {
            mensaje = "La temperatura es de " + temperatura + "°C. Es un día muy lindo.";
        }
        return mensaje;
    }

    private void enviarNotificacion(String mensaje) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Configurar la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.nuve) // Usa el ícono de nube aquí
                .setContentTitle("Advertencia del clima")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Eliminar la notificación al hacer clic en ella

        // Enviar la notificación
        manager.notify(1, builder.build());
    }
}
