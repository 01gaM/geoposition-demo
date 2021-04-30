package com.example.demo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class UpdateService extends Service {
    private Notification notification;
    private static LocationProvider locationProvider;
    private PendingIntent pendingIntent;
    private static LatLng location;
    private double interval;
    private static Exception currException;
    private boolean showSpeed;
    private Class<?> activityClassName;

    public void onCreate() {
        super.onCreate();
        locationProvider = new LocationProvider(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        pendingIntent = intent.getParcelableExtra("pendingIntent");
        interval = intent.getDoubleExtra("intervalMin", 1);
        showSpeed = intent.getBooleanExtra("showSpeed", false);
        if (showSpeed) {
            activityClassName = SpeedActivity.class;
        } else {
            activityClassName = UpdateCoordinatesActivity.class;
        }
        updateCoordinatesTask();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification(String title, LatLng coordinates) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MY_CHANNEL_ID",
                    "MY_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("MY_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }

        String contentText;
        if (showSpeed) {
            contentText = "Скорость: " + coordinates.getSpeed() + " м/с";
        } else {
            contentText = coordinates.toString();
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "MY_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(contentText)// message for notification
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), activityClassName);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        return mBuilder.build();
    }

    private void updateCoordinatesTask() {
        ILocationCallback myCallback = new ILocationCallback() {
            @Override
            public void callOnSuccess(LatLng lastUpdatedLocation) {
                notification = createNotification("Выполняется обновление координат...", lastUpdatedLocation);
                UpdateService.location = lastUpdatedLocation;
                startForeground(1, notification);
                try {
                    pendingIntent.send(0);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void callOnFail(Exception e) {
                stopForeground(true);
                try {
                    pendingIntent.send(1);
                } catch (PendingIntent.CanceledException canceledException) {
                    canceledException.printStackTrace();
                }
            }
        };

        try {
            locationProvider.requestLocationUpdates(interval, myCallback);
        } catch (NoLocationAccessException e) {
            stopForeground(true);
            currException = e;
            try {
                pendingIntent.send(2);
            } catch (PendingIntent.CanceledException canceledException) {
                canceledException.printStackTrace();
            }
        } catch (LocationProviderDisabledException | IntervalValueOutOfRangeException e) {
            stopForeground(true);
            currException = e;
            try {
                pendingIntent.send(1);
            } catch (PendingIntent.CanceledException canceledException) {
                canceledException.printStackTrace();
            }
        }
    }

    public static LatLng getLocation() {
        return UpdateService.location;
    }

    public static LocationProvider getLocationProvider() {
        return UpdateService.locationProvider;
    }

    public static Exception getCurrException() {
        return UpdateService.currException;
    }
}
