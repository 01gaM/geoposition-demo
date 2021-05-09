package com.example.geopositionmodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import com.example.geopositionmodule.exceptions.DeviceLocationDisabledException;
import com.example.geopositionmodule.exceptions.EmptyLocationCacheException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * A location provider that uses Fused Location Provider API from Google Play services Location library (supports devices with Google Play services only)
 */
public class LocationProviderClientGoogleAPI extends LocationProviderClient {
    private final FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context); //an instance of a Fused Location Provider API Client
    private LocationCallback updateLocationCallback = null; //a callback used in requestLocationUpdates()
    private CancellationTokenSource currLocationCancellationToken;

    protected LocationProviderClientGoogleAPI(Context context) {
        super(context);
    }

    public LocationCallback getUpdateLocationCallback() {
        return updateLocationCallback;
    }


    /**
     * This method requests last known location from {@link FusedLocationProviderClient}.
     * Result is being retrieved by calling {@link FusedLocationProviderClient#getLastLocation()} method.
     *
     * @param callback A callback to which a result as a LatLng instance is being passed to.
     * @throws EmptyLocationCacheException           Exception is thrown when null last location found due to empty location cache:
     *                                               - Location is turned off in the device settings (disabling location clears the cache)
     *                                               - The device never recorded its location (a new device or a device that has been restored to factory settings)
     *                                               - Google Play services on the device has restarted, and there is no active Fused Location Provider client that has requested location.
     * @throws LocationPermissionNotGrantedException Exception is thrown when location permission is not granted.
     * @throws LocationProviderDisabledException     Exception is thrown when both GPS and network location providers are disabled.
     */
    @Override
    public void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location == null) {
                                callback.callOnFail(new EmptyLocationCacheException());
                            } else {
                                callback.callOnSuccess(new LatLng(location));
                            }
                        }
                    });
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    /**
     * This method requests current known location from {@link FusedLocationProviderClient}.
     * This method may return locations that are a few seconds old, but never returns much older locations.
     * This is suitable for foreground applications that need a single fresh current location.
     * Expiration duration of returned location is being set to 30 seconds in {@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method.
     * ({@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method was introduced in play-services-location:17.1.0)
     *
     * @param callback A callback to which a result as a LatLng instance is being passed to.
     * @throws NullPointerException                  Exception is thrown if the device location can't be determined within reasonable time (tens of seconds)
     * @throws LocationPermissionNotGrantedException Exception is thrown when both {@link Manifest.permission#ACCESS_FINE_LOCATION}
     *                                               and {@link Manifest.permission#ACCESS_COARSE_LOCATION} and not granted.
     * @throws LocationProviderDisabledException     Exception is thrown when both GPS and network location providers are disabled.
     */
    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, DeviceLocationDisabledException {
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currLocationCancellationToken = new CancellationTokenSource();
            this.fusedLocationProviderClient.getCurrentLocation(accuracyPriority.getCode(), currLocationCancellationToken.getToken())
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location == null) {
                                handleRequestFailure(callback);
                            } else {
                                callback.callOnSuccess(new LatLng(location));
                            }
                        }
                    });
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    /**
     * This method requests location updates from {@link FusedLocationProviderClient}.
     * Calls ({@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method.
     *
     * @param intervalMin Location update interval value in minutes.
     * @param callback    A callback to which a result as a LatLng instance is being passed to.
     * @throws NullPointerException                  Exception is thrown when the device location can't be determined within reasonable time (tens of seconds)
     * @throws LocationPermissionNotGrantedException Exception is thrown when location permission is not granted.
     * @throws LocationProviderDisabledException     Exception is thrown when both GPS and network location providers are disabled.
     * @throws IntervalValueOutOfRangeException      Exception is thrown when input interval value is out of range.
     */
    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, IntervalValueOutOfRangeException, DeviceLocationDisabledException {
        checkUpdateIntervalValue(intervalMin);
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            long millis = (long) (intervalMin * 60 * 1000);
            locationRequest.setInterval(millis);
            locationRequest.setFastestInterval(millis);
            locationRequest.setPriority(accuracyPriority.getCode());

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);

            updateLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    callback.callOnSuccess(new LatLng(locationResult.getLastLocation()));
                }

                @Override
                public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                    if (!locationAvailability.isLocationAvailable()) {
                        handleRequestFailure(callback);
                        stopLocationUpdates();
                    }
                }
            };
            this.fusedLocationProviderClient.requestLocationUpdates(locationRequest, updateLocationCallback, Looper.getMainLooper());
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    /**
     * This method checks whether there is an active location update started by {@link #requestLocationUpdates(double, ILocationCallback)} method and stops it.
     * {@link #updateLocationCallback is null only when there is no active location updates}.
     */
    @Override
    public void stopLocationUpdates() {
        if (updateLocationCallback != null) {
            this.fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
            updateLocationCallback = null;
        }
    }

    @Override
    public void cancelCurrentLocationRequest() {
        if (currLocationCancellationToken != null) {
            currLocationCancellationToken.cancel();
            currLocationCancellationToken = null;
        }
    }
}
