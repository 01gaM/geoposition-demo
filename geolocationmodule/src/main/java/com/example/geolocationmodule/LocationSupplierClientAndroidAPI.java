
package com.example.geolocationmodule;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.example.geolocationmodule.exceptions.AirplaneModeOnException;
import com.example.geolocationmodule.exceptions.DeviceLocationDisabledException;
import com.example.geolocationmodule.exceptions.EmptyLocationCacheException;
import com.example.geolocationmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geolocationmodule.exceptions.LocationProviderDisabledException;
import com.example.geolocationmodule.exceptions.LocationPermissionNotGrantedException;
import com.example.geolocationmodule.exceptions.NetworkUpdateIntervalOutOfRangeException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * A location provider that uses LocationManager from Android Location API (supports devices with no Google Play services)
 */
public class LocationSupplierClientAndroidAPI extends LocationSupplierClient {
    private final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    private LocationListener updateLocationListener;
    private LocationListener currLocationListener;
    private CountDownTimer updateLocationTimer = null;
    private CountDownTimer currLocationTimer = null;
    /**
     * The minimum location update interval in minutes (equals 0.33 minutes ~ 20 seconds) used instead of {@link LocationSupplier#MINIMUM_UPDATE_INTERVAL}
     * when {@link #locationManager} requests {@link #requestLocationUpdates(double, ILocationCallback)} with {@link LocationManager#NETWORK_PROVIDER}
     * Used for {@link #requestLocationUpdates(double, ILocationCallback)} method
     */
    public static final double MINIMUM_UPDATE_INTERVAL_NETWORK = 0.33;
    private final long REQUEST_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private BroadcastReceiver airplaneModeUpdatesReceiver = null;

    public LocationSupplierClientAndroidAPI(Context context) {
        super(context);
    }

    private CountDownTimer createTimeoutTimer(ILocationCallback callback) {
        return new CountDownTimer(REQUEST_TIMEOUT_MILLIS, TimeUnit.SECONDS.toMillis(5)) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                callback.callOnFail(new LocationProviderDisabledException());
                stopLocationUpdates();
            }
        };
    }

    public LocationListener getUpdateLocationListener() {
        return updateLocationListener;
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        switch (accuracyPriority) {
            case PRIORITY_LOW_POWER:
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAccuracy(Criteria.NO_REQUIREMENT);
                break;
            case PRIORITY_BALANCED_POWER_ACCURACY:
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                break;
            default:
                //PRIORITY_HIGH_ACCURACY
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                break;
        }
        return criteria;
    }

    private String getAvailableProviderName() throws LocationProviderDisabledException, AirplaneModeOnException {
        Criteria criteria = getCriteria();
        String providerName = locationManager.getBestProvider(criteria, true);
        //If no suitable enabled provider is found, providerName is null
        if (providerName == null) {
            throw new LocationProviderDisabledException();
        }
        if (providerName.equals(LocationManager.NETWORK_PROVIDER)) {
            checkAirplaneModeOff();
        }
        return providerName;
    }

    /**
     * This method returns last known location from a cached location value.
     * This will never activate sensors to compute a new location, and will only ever return a cached location.
     *
     * @throws NullPointerException Exception is thrown when location is null.
     */
    @Override
    public void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException {
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
                callback.callOnSuccess(new LatLng(location));
            } else {
                callback.callOnFail(new EmptyLocationCacheException()); //null last location found due to empty location cache
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
            if (providerName.equals(LocationManager.NETWORK_PROVIDER)) {
                checkAirplaneModeStatus(callback);
            }
            currLocationTimer = createTimeoutTimer(callback);
            currLocationListener = getLocationListener(callback, true);
            locationManager.requestLocationUpdates(providerName,
                    TimeUnit.MINUTES.toMillis(0),
                    0,
                    currLocationListener);
            currLocationTimer.start();
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    /**
     * This method checks whether the input interval value in minutes is out of range or not.
     *
     * @param intervalMin An input value for {@link #requestLocationUpdates(double, ILocationCallback)} method.
     * @throws NetworkUpdateIntervalOutOfRangeException Exception is thrown when input value is
     *                                                  less than {@link #MINIMUM_UPDATE_INTERVAL_NETWORK}.
     */
    private void checkNetworkUpdateIntervalValue(double intervalMin) throws NetworkUpdateIntervalOutOfRangeException {
        if (intervalMin < MINIMUM_UPDATE_INTERVAL_NETWORK) {
            throw new NetworkUpdateIntervalOutOfRangeException();
        }
    }

    private void checkAirplaneModeStatus(ILocationCallback callback) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    checkAirplaneModeOff();
                } catch (AirplaneModeOnException e) {
                    //Airplane mode is on
                    callback.callOnFail(e);
                    stopLocationUpdates();
                }
            }
        };
        context.registerReceiver(receiver, intentFilter);
        airplaneModeUpdatesReceiver = receiver;
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, IntervalValueOutOfRangeException, DeviceLocationDisabledException, AirplaneModeOnException {
        checkLocationSettingsEnabled();
        checkUpdateIntervalValue(intervalMin);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            long intervalMillis = (long) (intervalMin * 60000);
            String providerName = getAvailableProviderName();
            if (providerName.equals(LocationManager.NETWORK_PROVIDER)) {
                checkNetworkUpdateIntervalValue(intervalMin);
                checkAirplaneModeStatus(callback);
            }
            updateLocationTimer = createTimeoutTimer(callback);
            updateLocationListener = getLocationListener(callback, false);
            locationManager.requestLocationUpdates(providerName,
                    intervalMillis,
                    0,
                    updateLocationListener);
            updateLocationTimer.start();
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    private LocationListener getLocationListener(ILocationCallback callback, boolean isSingleUpdate) {
        return new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                callback.callOnSuccess(new LatLng(location));
                if (isSingleUpdate) {
                    cancelCurrentLocationRequest();
                } else {
                    //restart timer
                    updateLocationTimer.cancel();
                    updateLocationTimer.start();
                }
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                handleRequestFailure(callback);
                if (isSingleUpdate) {
                    cancelCurrentLocationRequest();
                } else {
                    stopLocationUpdates();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
    }

    @Override
    public void stopLocationUpdates() {
        cancelRequest(updateLocationTimer, updateLocationListener);
        updateLocationListener = null;
    }

    @Override
    public void cancelCurrentLocationRequest() {
        cancelRequest(currLocationTimer, currLocationListener);
        currLocationListener = null;
    }

    private void cancelRequest(CountDownTimer timer, LocationListener locationListener) {
        if (airplaneModeUpdatesReceiver != null) {
            context.unregisterReceiver(airplaneModeUpdatesReceiver);
            airplaneModeUpdatesReceiver = null;
        }

        if (timer != null) {
            timer.cancel();
        }
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
