package com.example.geopositionmodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * A location provider that uses Fused Location Provider API from Google Play services Location library (supports devices with Google Play services only)
 */
public class LocationProviderClientGoogleAPI extends LocationProviderClient {
    private final FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context); //an instance of a Fused Location Provider API Client
    private LocationCallback updateLocationCallback = null; //a callback used in requestLocationUpdates()
    private int accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY.getCode(); //accuracyPriority used in fusedLocationProviderClient

    protected LocationProviderClientGoogleAPI(Context context) {
        super(context);
    }

    public LocationCallback getUpdateLocationCallback() {
        return updateLocationCallback;
    }

    public int getAccuracyPriority() {
        return accuracyPriority;
    }

    /**
     * This method allows to set a specific accuracy priority to a LocationProvider instance
     *
     * @param accuracyPriority A new accuracy priority value from {@link com.example.geopositionmodule.AccuracyPriority} enum that is to be set to {@link #accuracyPriority} field
     */
    public void setAccuracyPriority(AccuracyPriority accuracyPriority) {
        this.accuracyPriority = accuracyPriority.getCode();
    }

    /**
     * This method requests last known location from {@link FusedLocationProviderClient}.
     * If {@link #lastLocation} contains cached location value, than it'is considered as a result,
     * else result is being retrieved by calling {@link FusedLocationProviderClient#getLastLocation()} method.
     *
     * @param myLocationCallback A callback to which a result as a LatLng instance is being passed to.
     * @throws NullPointerException              Exception is thrown when null last location found:
     *                                           - Location is turned off in the device settings (disabling location clears the cache)
     *                                           - The device never recorded its location (a new device or a device that has been restored to factory settings)
     *                                           - Google Play services on the device has restarted, and there is no active Fused Location Provider client that has requested location.
     * @throws NoLocationAccessException         Exception is thrown when location permission is not granted.
     * @throws LocationProviderDisabledException Exception is thrown when both GPS and network location providers are disabled.
     */
    @Override
    public void getLastKnownLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted(context);
        checkLocationSettingsEnabled(context);
        if (LocationProviderClientGoogleAPI.lastLocation != null) {
            myLocationCallback.callOnSuccess(new LatLng(LocationProviderClientGoogleAPI.lastLocation));
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                this.fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location == null) {
                                    myLocationCallback.callOnFail(new NullPointerException("Последние координаты не были найдены (lastLocation = null)."));
                                } else {
                                    LocationProviderClientGoogleAPI.lastLocation = location;
                                    myLocationCallback.callOnSuccess(new LatLng(location));
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                myLocationCallback.callOnFail(e);
                            }
                        });
            }
        }
    }

    /**
     * This method requests current known location from {@link FusedLocationProviderClient}.
     * This method may return locations that are a few seconds old, but never returns much older locations.
     * This is suitable for foreground applications that need a single fresh current location.
     * Expiration duration of returned location is being set to 30 seconds in {@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method.
     * ({@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method was introduced in play-services-location:17.1.0)
     *
     * @param myLocationCallback A callback to which a result as a LatLng instance is being passed to.
     * @throws NullPointerException              Exception is thrown if the device location can't be determined within reasonable time (tens of seconds)
     * @throws NoLocationAccessException         Exception is thrown when location permission is not granted.
     * @throws LocationProviderDisabledException Exception is thrown when both GPS and network location providers are disabled.
     */
    @Override
    public void requestCurrentLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted(context);
        checkLocationSettingsEnabled(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.fusedLocationProviderClient.getCurrentLocation(accuracyPriority, null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location == null) {
                                try {
                                    checkLocationSettingsEnabled(context);
                                    myLocationCallback.callOnFail(new NullPointerException("Текущие координаты не были найдены (location = null)."));
                                } catch (LocationProviderDisabledException e) {
                                    myLocationCallback.callOnFail(e);
                                }
                            } else {
                                LocationProviderClientGoogleAPI.lastLocation = location;
                                myLocationCallback.callOnSuccess(new LatLng(location));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            myLocationCallback.callOnFail(e);
                        }
                    });
        }
    }

    /**
     * This method requests location updates from {@link FusedLocationProviderClient}.
     * Calls ({@link FusedLocationProviderClient#getCurrentLocation(int, CancellationToken)} method.
     *
     * @param intervalMin        Location update interval value in minutes.
     * @param myLocationCallback A callback to which a result as a LatLng instance is being passed to.
     * @throws NullPointerException              Exception is thrown when the device location can't be determined within reasonable time (tens of seconds)
     * @throws NoLocationAccessException         Exception is thrown when location permission is not granted.
     * @throws LocationProviderDisabledException Exception is thrown when both GPS and network location providers are disabled.
     * @throws IntervalValueOutOfRangeException  Exception is thrown when input interval value is out of range.
     */
    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback myLocationCallback) throws NoLocationAccessException, LocationProviderDisabledException, IntervalValueOutOfRangeException, NullPointerException {
        checkUpdateIntervalValue(intervalMin);
        checkPermissionGranted(context);
        checkLocationSettingsEnabled(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            long millis = (long) (intervalMin * 60 * 1000);
            locationRequest.setInterval(millis);
            locationRequest.setFastestInterval(millis);
            locationRequest.setPriority(accuracyPriority);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);

            updateLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationProviderClientGoogleAPI.lastLocation = locationResult.getLastLocation();
                    myLocationCallback.callOnSuccess(new LatLng(LocationProviderClientGoogleAPI.lastLocation));
                }

                @Override
                public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                    if (!locationAvailability.isLocationAvailable()) {
                        try {
                            checkLocationSettingsEnabled(context);
                            myLocationCallback.callOnFail(new NullPointerException("Текущие координаты не были найдены (location = null)."));
                        } catch (LocationProviderDisabledException e) {
                            myLocationCallback.callOnFail(e);
                        }
                        stopLocationUpdates();
                    }
                }
            };
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                this.fusedLocationProviderClient.requestLocationUpdates(locationRequest, updateLocationCallback, Looper.getMainLooper());
            }
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
}
