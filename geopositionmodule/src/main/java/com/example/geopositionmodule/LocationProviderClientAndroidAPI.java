
package com.example.geopositionmodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.DeviceLocationDisabledException;
import com.example.geopositionmodule.exceptions.EmptyLocationCacheException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;
import com.example.geopositionmodule.exceptions.NetworkUpdateIntervalOutOfRangeException;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * A location provider that uses LocationManager from Android Location API (supports devices with no Google Play services)
 */
public class LocationProviderClientAndroidAPI extends LocationProviderClient {
    private final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    private LocationListener updateLocationListener;
    /**
     * The minimum location update interval in minutes (equals 0.33 minutes ~ 20 seconds) used instead of {@link #MINIMUM_UPDATE_INTERVAL_NETWORK}
     * when {@link #locationManager} requests {@link #requestLocationUpdates(double, ILocationCallback)} with {@link LocationManager#NETWORK_PROVIDER}
     * Used for {@link #requestLocationUpdates(double, ILocationCallback)} method
     */
    public static final double MINIMUM_UPDATE_INTERVAL_NETWORK = 0.33;

    public LocationProviderClientAndroidAPI(Context context) {
        super(context);
    }

    public LocationListener getUpdateLocationListener() {
        return updateLocationListener;
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        switch (accuracyPriority) {
            case PRIORITY_LOW_POWER:
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                break;
            case PRIORITY_BALANCED_POWER_ACCURACY:
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                break;
            case PRIORITY_HIGH_ACCURACY:
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                break;
        }
        return criteria;
    }

    private String getAvailableProviderName() throws LocationProviderDisabledException, AirplaneModeOnException {
        Criteria criteria = getCriteria();
        String providerName = locationManager.getBestProvider(criteria, true);
        // If no suitable enabled provider is found, null is returned
        List<String> list = locationManager.getAllProviders();
        //locationManager.getProvider(providerName).requiresNetwork() TODO: try tris way
        if (providerName.equals(LocationManager.NETWORK_PROVIDER)) {
            checkAirplaneModeOff();
        }
        if (providerName != null) {
            return providerName;
        }
        throw new LocationProviderDisabledException(); //Доступный подходящий провайдер данных о местоположении не найден
    }

    /**
     * This method returns last known location from a cached location value.
     * This will never activate sensors to compute a new location, and will only ever return a cached location.
     *
     * @throws NullPointerException Exception is thrown when location is null.
     */
    @Override
    public void getLastKnownLocation(ILocationCallback myLocationCallback) throws LocationPermissionNotGrantedException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<String> providerNames = locationManager.getAllProviders();
            Location location = null;
            float bestAccuracy = Float.MAX_VALUE;
            for (String currProvider : providerNames) {
                Location currLocation = locationManager.getLastKnownLocation(currProvider);
                if (currLocation != null) {
                    float currAccuracy = currLocation.getAccuracy();
                    if (currAccuracy < bestAccuracy) {
                        bestAccuracy = currAccuracy;
                        location = currLocation;
                    }
                }
            }
            if (location != null) {
                myLocationCallback.callOnSuccess(new LatLng(location));
            } else {
                myLocationCallback.callOnFail(new EmptyLocationCacheException()); //null last location found due to empty location cache
            }
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, DeviceLocationDisabledException, AirplaneModeOnException, LocationProviderDisabledException {
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            String providerName = getAvailableProviderName();
            updateLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    callback.callOnSuccess(new LatLng(location));
                    stopLocationUpdates();
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    handleRequestFailure(callback);
                    stopLocationUpdates();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };
            locationManager.requestLocationUpdates(providerName,
                    TimeUnit.MINUTES.toMillis(0),
                    0,
                    updateLocationListener);
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    /**
     * This method checks whether the input interval value in minutes is out of range or not.
     *
     * @param intervalMin An input value for {@link #requestLocationUpdates(double, ILocationCallback)} method.
     * @throws IntervalValueOutOfRangeException Exception is thrown when input value is
     *                                          less than {@link com.example.geopositionmodule.LocationProvider#MINIMUM_UPDATE_INTERVAL} or
     *                                          more than {@link com.example.geopositionmodule.LocationProvider#MINIMUM_UPDATE_INTERVAL}.
     */
    private void checkNetworkUpdateIntervalValue(double intervalMin, String providerName) throws NetworkUpdateIntervalOutOfRangeException {
        if (providerName.equals(LocationManager.NETWORK_PROVIDER) && intervalMin < MINIMUM_UPDATE_INTERVAL_NETWORK) {
            throw new NetworkUpdateIntervalOutOfRangeException();
        }
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, IntervalValueOutOfRangeException, DeviceLocationDisabledException, AirplaneModeOnException {
        checkLocationSettingsEnabled();
        checkUpdateIntervalValue(intervalMin);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            long intervalMillis = (long) (intervalMin * 60000);
            String providerName = getAvailableProviderName();
            checkNetworkUpdateIntervalValue(intervalMin, providerName);
            updateLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    callback.callOnSuccess(new LatLng(location));
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    handleRequestFailure(callback);
                    stopLocationUpdates();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };
            locationManager.requestLocationUpdates(providerName,
                    intervalMillis,
                    0,
                    updateLocationListener);
        } else {
            throw new LocationPermissionNotGrantedException();
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
