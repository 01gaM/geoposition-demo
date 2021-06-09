package com.example.geolocationmodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import com.example.geolocationmodule.exceptions.DeviceLocationDisabledException;
import com.example.geolocationmodule.exceptions.EmptyLocationCacheException;
import com.example.geolocationmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geolocationmodule.exceptions.LocationPermissionNotGrantedException;
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
public class LocationSupplierClientGoogleAPI extends LocationSupplierClient {
    private final FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context); //an instance of a Fused Location Provider API Client
    private LocationCallback updateLocationCallback = null; //a callback used in requestLocationUpdates()
    private CancellationTokenSource currLocationCancellationToken;

    protected LocationSupplierClientGoogleAPI(Context context) {
        super(context);
    }

    public LocationCallback getUpdateLocationCallback() {
        return updateLocationCallback;
    }

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
     * {@inheritDoc}
     * This method requests current location from {@link FusedLocationProviderClient}.
     * This method may return locations that are a few seconds old, but never returns much older locations.
     * Expiration duration of returned location is being set to 30 seconds in {@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method.
     * ({@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method was introduced in play-services-location:17.1.0)
     */
    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, DeviceLocationDisabledException {
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currLocationCancellationToken = new CancellationTokenSource();
            this.fusedLocationProviderClient.getCurrentLocation(accuracyPriority.code, currLocationCancellationToken.getToken())
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

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, IntervalValueOutOfRangeException, DeviceLocationDisabledException {
        checkUpdateIntervalValue(intervalMin);
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//        try {
            LocationRequest locationRequest = LocationRequest.create();
            long millis = (long) (intervalMin * 60 * 1000);
            locationRequest.setInterval(millis);
            locationRequest.setFastestInterval(millis);
            locationRequest.setPriority(accuracyPriority.code);

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
//        } catch (SecurityException e){
//            stopLocationUpdates();
//            throw new LocationPermissionNotGrantedException();
//        }
        } else {
            throw new LocationPermissionNotGrantedException();
        }
    }

    /**
     * Checks whether there is an active location update started by {@link #requestLocationUpdates(double, ILocationCallback)} method and stops it
     * {@link #updateLocationCallback} is null only when there is no active location updates.
     */
    @Override
    public void stopLocationUpdates() {
        if (updateLocationCallback != null) {
            this.fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
            updateLocationCallback = null;
        }
    }

    /**
     * Checks whether there is an active current location request started by {@link #requestCurrentLocation(ILocationCallback)} method and stops it
     * {@link #currLocationCancellationToken} is null only when there is no active current location request.
     */
    @Override
    public void cancelCurrentLocationRequest() {
        if (currLocationCancellationToken != null) {
            currLocationCancellationToken.cancel();
            currLocationCancellationToken = null;
        }
    }
}
