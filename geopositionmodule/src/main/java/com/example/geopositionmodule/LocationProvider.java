package com.example.geopositionmodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.core.app.ActivityCompat;


public class LocationProvider implements ILocationProvider {
    private static Location lastLocation = null;
    private Activity activity;
    private static FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback updateLocationCallback = null;
    private int accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY.getCode();


    public LocationProvider(Activity activity) throws GooglePlayServicesNotAvailableException {
        this.activity = activity;
        if (googlePlayServicesAvailable()) {
            LocationProvider.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationProvider.fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
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

    private boolean checkLocationSettingsEnabled() throws LocationProviderDisabledException {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gpsEnabled || networkEnabled) {
            return true;
        }
        throw new LocationProviderDisabledException();
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException {
        if (!isPermissionGranted()) {
            throw new NoLocationAccessException();
        }
        if (LocationProvider.lastLocation != null) {
            return new LatLng(LocationProvider.lastLocation);
        } else {
            //request here
        }
        throw new NullPointerException("Последние координаты не были найдены (lastLocation = null).");
    }

    @Override
    public void requestCurrentLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new NoLocationAccessException();
        }
        if (checkLocationSettingsEnabled()) {
            LocationProvider.fusedLocationProviderClient.getCurrentLocation(accuracyPriority, null).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LocationProvider.lastLocation = location;
                    myLocationCallback.callbackCall(new LatLng(location));
                }
            });
        }
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback myLocationCallback) throws NoLocationAccessException, LocationProviderDisabledException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new NoLocationAccessException();
        }
        if (checkLocationSettingsEnabled()) {
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
                    myLocationCallback.callbackCall(new LatLng(LocationProvider.lastLocation));
                }
            };
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationProvider.fusedLocationProviderClient.requestLocationUpdates(locationRequest, updateLocationCallback, Looper.getMainLooper());
            }
        }
    }

    public void stopLocationUpdates() {
        if (updateLocationCallback != null) {
            LocationProvider.fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
        }
    }
}
