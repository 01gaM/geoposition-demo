package com.example.geopositionmodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * A location provider that uses LocationManager from Android Location API (supports devices with no Google Play services)
 */
public class LocationProviderClientAndroidAPI extends LocationProviderClient {
    private final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    private LocationListener updateLocationListener;

    public LocationProviderClientAndroidAPI(Context context) {
        super(context);
    }

    public LocationListener getUpdateLocationListener() {
        return updateLocationListener;
    }

    private String getAvailableProviderName() throws NullPointerException {
        // Retrieve a list of location providers that have fine accuracy, no monetary cost, etc
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //TODO: check this
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String providerName = locationManager.getBestProvider(criteria, true);

        // If no suitable provider is found, null is returned.
        if (providerName != null) {
            return providerName;
        }
        throw new NullPointerException("Доступный подходящий провайдер данных о местоположении не найден.");
    }

    /**
     * This method returns last known location from {@link #lastLocation} if it is not null and contains a cached location value.
     * This will never activate sensors to compute a new location, and will only ever return a cached location.
     *
     * @throws NullPointerException Exception is thrown when {@link #lastLocation} is null.
     */
    @Override
    public void getLastKnownLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted(context);
        checkLocationSettingsEnabled(context);
        if (LocationProviderClientAndroidAPI.lastLocation != null) {
            myLocationCallback.callOnSuccess(new LatLng(LocationProviderClientAndroidAPI.lastLocation));
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                String providerName = getAvailableProviderName();
                Location location = locationManager.getLastKnownLocation(providerName);
                if (location != null) {
                    LocationProviderClientAndroidAPI.lastLocation = location;
                    myLocationCallback.callOnSuccess(new LatLng(LocationProviderClientAndroidAPI.lastLocation));
                } else {
                    throw new NullPointerException("Последние координаты не были найдены (lastLocation = null).");
                }
            }
        }
    }

    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted(context);
        checkLocationSettingsEnabled(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            String providerName = getAvailableProviderName();
            updateLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    lastLocation = location;
                    callback.callOnSuccess(new LatLng(location));
                    stopLocationUpdates();
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    callback.callOnFail(new LocationProviderDisabledException());
                    stopLocationUpdates();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };
            locationManager.requestLocationUpdates(providerName,
                    TimeUnit.MINUTES.toMillis(100),
                    5,             // 5 meters
                    updateLocationListener);
        }
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws NoLocationAccessException, LocationProviderDisabledException, IntervalValueOutOfRangeException {
        checkPermissionGranted(context);
        checkLocationSettingsEnabled(context);
        checkUpdateIntervalValue(intervalMin);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            long intervalMillis = (long) (intervalMin * 60000);
            String providerName = getAvailableProviderName();
            updateLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    lastLocation = location;
                    callback.callOnSuccess(new LatLng(location));
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    callback.callOnFail(new LocationProviderDisabledException());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };
            locationManager.requestLocationUpdates(providerName,
                    intervalMillis,
                    0,
                    updateLocationListener);
        }
    }

    @Override
    public void stopLocationUpdates() {
        if (updateLocationListener != null) {
            locationManager.removeUpdates(updateLocationListener);
            updateLocationListener = null;
        }
    }
}
