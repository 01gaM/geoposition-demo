package com.example.demo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.example.geopositionmodule.AccuracyPriority;
import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class UpdateService extends Service {
    private static LocationProvider locationProvider;
    private PendingIntent pendingIntent;
    private static LatLng location;
    private double interval;
    private static Exception currException;
    private boolean showSpeed;
    private Class<?> activityClassName;
    public static final int UPDATE_SUCCEEDED = 0;
    public static final int UPDATE_FAILED = 1;
    public static final int LOCATION_PERMISSION_NOT_GRANTED = 2;

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
        AccuracyPriority accuracyPriority = (AccuracyPriority) intent.getSerializableExtra("accuracyPriority");
        if (accuracyPriority == null){
            accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY;
        }
        locationProvider.setAccuracyPriority(accuracyPriority);
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

    private void setNotificationText(String title, String contentText) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MY_CHANNEL_ID",
                    "MY_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("MY_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
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
        startForeground(1, mBuilder.build());
    }

    private void setLocationNotification(LatLng coordinates) {
        String contentText;
        if (showSpeed) {
            contentText = "Скорость: " + coordinates.getSpeed() + " м/с";
        } else {
            contentText = coordinates.toString();
        }
        setNotificationText("Выполняется обновление координат...", contentText);
    }

    private void updateCoordinatesTask() {
        ILocationCallback myCallback = new ILocationCallback() {
            @Override
            public void callOnSuccess(LatLng lastUpdatedLocation) {
                setLocationNotification(lastUpdatedLocation);
                UpdateService.location = lastUpdatedLocation;
                try {
                    pendingIntent.send(UPDATE_SUCCEEDED);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void callOnFail(Exception e) {
                currException = e;
                setNotificationText("Ошибка!", e.getMessage());
                stopForeground(false);
                try {
                    pendingIntent.send(UPDATE_FAILED);
                } catch (PendingIntent.CanceledException canceledException) {
                    canceledException.printStackTrace();
                }
            }
        };

        try {
            locationProvider.requestLocationUpdates(interval, myCallback);
        } catch (LocationPermissionNotGrantedException e) {
            currException = e;
            stopForeground(true);
            try {
                pendingIntent.send(LOCATION_PERMISSION_NOT_GRANTED);
            } catch (PendingIntent.CanceledException canceledException) {
                canceledException.printStackTrace();
            }
        } catch (LocationProviderDisabledException | IntervalValueOutOfRangeException | AirplaneModeOnException e) {
            currException = e;
            stopForeground(true);
            try {
                pendingIntent.send(UPDATE_FAILED);
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
