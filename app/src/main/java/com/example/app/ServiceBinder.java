package com.example.app;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.geopositionmodule.LocationProvider;

public abstract class ServiceBinder extends BaseCoordinatesActivity {
    protected Intent intent;
    protected UpdateService updateService;
    protected boolean mShouldUnbind;
    protected int requestCode = 1;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            updateService = ((UpdateService.LocalBinder) service).getService();
            locationProvider = new LocationProvider(getApplicationContext());
            updateService.setLocationProvider(locationProvider);
            PendingIntent pendingIntent = createPendingResult(requestCode, new Intent(), 0);
            intent = new Intent(getApplicationContext(), UpdateService.class)
                    .putExtra("pendingIntent", pendingIntent);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never see this happen.
            updateService = null;
        }
    };

    protected void doBindService() {
        // Attempts to establish a connection with the service.  We use an explicit class name because we want a specific service
        // implementation that we know will be running in our own process (and thus won't be supporting component replacement by other applications).
        if (bindService(new Intent(getApplicationContext(), UpdateService.class), mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        }
    }

    protected void doUnbindService() {
        // Release information about the service's state.
        if (mShouldUnbind) {
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }

    protected void setRequestCode(int requestCode){
        this.requestCode = requestCode;
    }
}
