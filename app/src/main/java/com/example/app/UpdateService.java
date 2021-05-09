package com.example.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationSupplier;
import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.DeviceLocationDisabledException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class UpdateService extends Service {
    private LocationSupplier locationSupplier;
    private PendingIntent pendingIntent;
    private LatLng location;
    private double interval;
    private Exception currException;
    private boolean showSpeed;
    private Class<?> activityClassName;
    public static final int UPDATE_SUCCEEDED = 0;
    public static final int UPDATE_FAILED = 1;
    public static final int LOCATION_PERMISSION_NOT_GRANTED = 2;
    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        UpdateService getService() {
            return UpdateService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
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
        return mBinder;
    }

    private void setNotificationText(String title, String contentText) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "MY_CHANNEL_ID";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "LOCATION_UPDATE_SERVICE_CHANNEL",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Notifications from service that calls location updates from \"geopositionmodule\"");
            mNotificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
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
                location = lastUpdatedLocation;
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
            locationSupplier.requestLocationUpdates(interval, myCallback);
        } catch (LocationPermissionNotGrantedException e) {
            currException = e;
            stopForeground(true);
            try {
                pendingIntent.send(LOCATION_PERMISSION_NOT_GRANTED);
            } catch (PendingIntent.CanceledException canceledException) {
                canceledException.printStackTrace();
            }
        } catch (LocationProviderDisabledException | IntervalValueOutOfRangeException | AirplaneModeOnException | DeviceLocationDisabledException e) {
            currException = e;
            stopForeground(true);
            try {
                pendingIntent.send(UPDATE_FAILED);
            } catch (PendingIntent.CanceledException canceledException) {
                canceledException.printStackTrace();
            }
        }
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocationSupplier(LocationSupplier locationSupplier) {
        this.locationSupplier = locationSupplier;
    }

    public Exception getCurrException() {
        return currException;
    }
}
