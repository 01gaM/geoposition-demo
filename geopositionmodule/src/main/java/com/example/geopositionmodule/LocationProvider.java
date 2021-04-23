package com.example.geopositionmodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import static android.content.Context.LOCATION_SERVICE;


public class LocationProvider implements ILocationProvider {
    private static Location lastLocation = null; //last known location
    private final Context context; //the context in which this LocationProvider instance was created
    private final FusedLocationProviderClient fusedLocationProviderClient; //an instance of a Fused Location Provider API Client
    private LocationCallback updateLocationCallback = null; //a callback used in requestLocationUpdates()
    private int accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY.getCode(); //accuracyPriority used in fusedLocationProviderClient
    /**
     * The minimum location update interval in minutes (equals 0.05 minutes = 3 seconds)
     * Used in {@link #checkUpdateIntervalValue(double intervalMin)}
     */
    public static final double MINIMUM_UPDATE_INTERVAL = 0.05;


    public LocationProvider(Context context) throws GooglePlayServicesNotAvailableException {
        this.context = context;
        checkGooglePlayServicesAvailable();
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LocationProvider.lastLocation = location;
                }
            });
        }
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
     * This method checks whether the Google Play services APK is available and up-to-date on this device or not.
     *
     * @throws GooglePlayServicesNotAvailableException Exception is thrown when the Google Play services APK is not available or it's version is out-of-date on this device.
     */
    private void checkGooglePlayServicesAvailable() throws GooglePlayServicesNotAvailableException {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            //Google Play Services is missing or update is required
            throw new GooglePlayServicesNotAvailableException(resultCode);
        }
    }

    /**
     * This method checks whether the location settings are enabled on the device or not.
     *
     * @throws LocationProviderDisabledException Exception is thrown when both GPS and network location providers are disabled.
     */
    private void checkLocationSettingsEnabled() throws LocationProviderDisabledException {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !networkEnabled) {
            throw new LocationProviderDisabledException();
        }
    }

    /**
     * This method checks whether the location access permission is granted for the app or not.
     *
     * @throws NoLocationAccessException Exception is thrown when both {@link Manifest.permission#ACCESS_FINE_LOCATION}
     *                                   and {@link Manifest.permission#ACCESS_COARSE_LOCATION} and not granted.
     */
    private void checkPermissionGranted() throws NoLocationAccessException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            throw new NoLocationAccessException();
    }


    //TODO: check this method
    @Override
    public LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException {
        checkPermissionGranted();
        if (LocationProvider.lastLocation != null) {
            return new LatLng(LocationProvider.lastLocation);
        } else {
            //request here
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            //Criteria criteria = new Criteria();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    LocationProvider.lastLocation = location;
                    return new LatLng(LocationProvider.lastLocation);
                }
            }
        }
        throw new NullPointerException("Последние координаты не были найдены (lastLocation = null).");
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
    public void getLastKnownLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted();
        checkLocationSettingsEnabled();
        if (LocationProvider.lastLocation != null) {
            myLocationCallback.callOnSuccess(new LatLng(LocationProvider.lastLocation));
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
                                    LocationProvider.lastLocation = location;
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
        checkPermissionGranted();
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.fusedLocationProviderClient.getCurrentLocation(accuracyPriority, null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location == null) {
                                try {
                                    checkLocationSettingsEnabled();
                                    myLocationCallback.callOnFail(new NullPointerException("Текущие координаты не были найдены (location = null)."));
                                } catch (LocationProviderDisabledException e) {
                                    myLocationCallback.callOnFail(e);
                                }
                            } else {
                                LocationProvider.lastLocation = location;
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
     * This method checks whether the input interval value in minutes is out of range or not.
     *
     * @param intervalMin An input value for {@link #requestLocationUpdates(double, ILocationCallback)} method.
     * @throws IntervalValueOutOfRangeException Exception is thrown when input value is less than {@link #MINIMUM_UPDATE_INTERVAL}.
     */
    private void checkUpdateIntervalValue(double intervalMin) throws IntervalValueOutOfRangeException {
        if (intervalMin < LocationProvider.MINIMUM_UPDATE_INTERVAL) {
            throw new IntervalValueOutOfRangeException();
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
        checkPermissionGranted();
        checkLocationSettingsEnabled();
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
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationProvider.lastLocation = locationResult.getLastLocation();
                    myLocationCallback.callOnSuccess(new LatLng(LocationProvider.lastLocation));
                }

                @Override
                public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                    if (!locationAvailability.isLocationAvailable()) {
                        try {
                            checkLocationSettingsEnabled();
                            myLocationCallback.callOnFail(new NullPointerException("Текущие координаты не были найдены (location = null)."));
                        } catch (LocationProviderDisabledException e) {
                            myLocationCallback.callOnFail(e);
                        }
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
    public void stopLocationUpdates() {
        if (updateLocationCallback != null) {
            this.fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
        }
    }
}
