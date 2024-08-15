package com.example.transmisiondigital.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.transmisiondigital.R;
import com.example.transmisiondigital.models.Evento;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class PusherService extends Service {
    private static final String CHANNEL_ID = "notificaciones";
    private static final Integer NOTIFICATION_ID = 2;
    SharedPreferences sharedPreferences;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        sharedPreferences = getSharedPreferences("sessionUser", MODE_PRIVATE);
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createForegroundNotification().build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PusherOptions options = new PusherOptions();
        options.setCluster("mt1");

        Pusher pusher = new Pusher("235a28e331d4c1247a0d", options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("Pusher", "State changed from " + change.getPreviousState() + " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("Pusher", "There was a problem connecting! code (" + code + "), message (" + message + "), exception(" + e + ")");
            }
        }, ConnectionState.ALL);

        if(sharedPreferences.getString("rol", "").equals("Administrador")){
            Channel channel = pusher.subscribe("notificaciones_admin");
            channel.bind("notificaciones_admin", new SubscriptionEventListener() {
                @Override
                public void onEvent(PusherEvent event) {
                    Log.i("Pusher", "Received event with data: " + event.getData());
                    Gson gson = new Gson();
                    Evento evento = gson.fromJson(event.getData(), Evento.class);
                    if (sharedPreferences.getString("rol", "").equals("Administrador")) {
                        showNotification(evento.getMessage());
                    }
                }
            });
        } else {
            Channel channel = pusher.subscribe("notificaciones");
            channel.bind("notificaciones", new SubscriptionEventListener() {
                @Override
                public void onEvent(PusherEvent event) {
                    Log.i("Pusher", "Received event with data: " + event.getData());
                    Gson gson = new Gson();
                    Evento evento = gson.fromJson(event.getData(), Evento.class);
                    if(sharedPreferences.getInt("idUser", 0) == evento.getTecnicoId())
                    {
                        showNotification(evento.getMessage());
                    }
                    //showNotification(evento.getMessage());
                }
            });
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(String message) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Transmisión Digital")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Log.e("Notification", "Permission for posting notifications not granted.");
        }
        return;
    }
    //Log.i("Notification", "Posting notification");
    notificationManager.notify(NOTIFICATION_ID, builder.build());
}

    private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        CharSequence name = "Notificaciones";
        String description = "Canal para notificaciones de Pusher";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
    private NotificationCompat.Builder createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Transmisión Digital")
                .setContentText("La aplicacion se esta ejecutando en segundo plano")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);
    }
}