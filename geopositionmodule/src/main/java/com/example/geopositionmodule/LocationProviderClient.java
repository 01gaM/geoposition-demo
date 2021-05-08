package com.example.geopositionmodule;

import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;

import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.DeviceLocationDisabledException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;

import androidx.core.location.LocationManagerCompat;

import static android.content.Context.LOCATION_SERVICE;

public abstract class LocationProviderClient implements ILocationProvider {
    protected final Context context;
    protected AccuracyPriority accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY;

    protected LocationProviderClient(Context context) {
        this.context = context;
    }

    /**
     * This method allows to set a specific accuracy priority to a LocationProvider instance
     *
     * @param accuracyPriority A new accuracy priority value from {@link com.example.geopositionmodule.AccuracyPriority} enum that is to be set to {@link #accuracyPriority} field
     */
    @Override
    public void setAccuracyPriority(AccuracyPriority accuracyPriority) {
        this.accuracyPriority = accuracyPriority;
    }

    public AccuracyPriority getAccuracyPriority() {
        return accuracyPriority;
    }

    /**
     * This method checks whether the location settings are enabled on the device or not.
     *
     * @throws DeviceLocationDisabledException Exception is thrown when both GPS and network location providers are disabled.
     */
    protected void checkLocationSettingsEnabled() throws DeviceLocationDisabledException {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !networkEnabled) {
            throw new DeviceLocationDisabledException();
        }
    }

    protected void checkAirplaneModeOff() throws AirplaneModeOnException {
        int airplaneSetting = Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
        if (!(airplaneSetting == 0)) {
            throw new AirplaneModeOnException();
        }
    }

    /**
     * This method checks whether the input interval value in minutes is out of range or not.
     *
     * @param intervalMin An input value for {@link #requestLocationUpdates(double, ILocationCallback)} method.
     * @throws IntervalValueOutOfRangeException Exception is thrown when input value is
     *                                          less than {@link LocationProvider#MINIMUM_UPDATE_INTERVAL} or
     *                                          more than {@link LocationProvider#MINIMUM_UPDATE_INTERVAL}.
     */
    protected void checkUpdateIntervalValue(double intervalMin) throws IntervalValueOutOfRangeException {
        if (intervalMin < LocationProvider.MINIMUM_UPDATE_INTERVAL || intervalMin > LocationProvider.MAXIMUM_UPDATE_INTERVAL) {
            throw new IntervalValueOutOfRangeException();
        }
    }

    protected void handleRequestFailure(ILocationCallback callback) {
        try {
            checkLocationSettingsEnabled();
            checkAirplaneModeOff();
            callback.callOnFail(new LocationProviderDisabledException());
        } catch (DeviceLocationDisabledException | AirplaneModeOnException e) {
            callback.callOnFail(e);
        }
    }
}
