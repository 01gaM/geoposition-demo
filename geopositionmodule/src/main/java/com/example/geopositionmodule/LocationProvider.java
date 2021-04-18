package com.example.geopositionmodule;

import android.Manifest;
import android.app.Activity;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static android.content.Context.LOCATION_SERVICE;


public class LocationProvider implements ILocationProvider {
    private static Location lastLocation = null;
    private Activity activity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback updateLocationCallback = null;
    private int accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY.getCode();


    public LocationProvider(Activity activity) throws GooglePlayServicesNotAvailableException {
        this.activity = activity;
        if (googlePlayServicesAvailable()) {
            this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                this.fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LocationProvider.lastLocation = location;
                    }
                });
            }
        }
    }

    public void setAccuracyPriority(AccuracyPriority accuracyPriority) {
        this.accuracyPriority = accuracyPriority.getCode();
    }


    private boolean googlePlayServicesAvailable() throws GooglePlayServicesNotAvailableException {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            //Google Play Services is missing or update is required
            throw new GooglePlayServicesNotAvailableException(resultCode);
        }
        return true;
    }

    private void checkLocationSettingsEnabled() throws LocationProviderDisabledException {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !networkEnabled) {
            throw new LocationProviderDisabledException();
        }
    }

    private void checkPermissionGranted() throws NoLocationAccessException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            throw new NoLocationAccessException();
    }

    @Override
    public LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException {
        checkPermissionGranted();
        if (LocationProvider.lastLocation != null) {
            return new LatLng(LocationProvider.lastLocation);
        } else {
            //request here
            LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
            //Criteria criteria = new Criteria();
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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


    public LatLng getLastKnownLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted();
        if (LocationProvider.lastLocation != null) {
            return new LatLng(LocationProvider.lastLocation);
        } else {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                this.fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LocationProvider.lastLocation = location;
                        myLocationCallback.callOnSuccess(new LatLng(location));
                    }
                });
            }
        }
        throw new NullPointerException("Последние координаты не были найдены (lastLocation = null).");
    }

    @Override
    public void requestCurrentLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted();
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.fusedLocationProviderClient.getCurrentLocation(accuracyPriority, null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            LocationProvider.lastLocation = location;
                            if (location == null) {
                                try {
                                    checkLocationSettingsEnabled();
                                    myLocationCallback.callOnFail(new NullPointerException("Текущие координаты не были найдены (location = null)."));
                                } catch (LocationProviderDisabledException e) {
                                    myLocationCallback.callOnFail(e);
                                }
                            } else {
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

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback myLocationCallback) throws NoLocationAccessException, LocationProviderDisabledException {
        checkPermissionGranted();
        checkLocationSettingsEnabled();
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                this.fusedLocationProviderClient.requestLocationUpdates(locationRequest, updateLocationCallback, Looper.getMainLooper());
            }
        }

    }

    public void stopLocationUpdates() {
        if (updateLocationCallback != null) {
            this.fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
        }
    }
}
