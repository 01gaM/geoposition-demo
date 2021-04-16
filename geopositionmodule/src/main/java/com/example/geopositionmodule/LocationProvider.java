package com.example.geopositionmodule;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
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

    public LocationProvider(Activity activity) {
        this.activity = activity;
        if (checkGooglePlayServices()) {
            LocationProvider.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        } else {
            //TODO: new exception
        }
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            //Google Play Services is missing or update is required
            final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
            apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            return false;
        }
        return true;
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocation() {
        if (!isPermissionGranted()) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }
    }

    @Override
    public LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException {
        enableMyLocation();
        if (!isPermissionGranted()) {
            throw new NoLocationAccessException();
        }
        if (LocationProvider.lastLocation != null) {
            return new LatLng(LocationProvider.lastLocation);
        }
        throw new NullPointerException("Последние координаты не были найдены (lastLocation = null).");
    }

    @Override
    public void requestCurrentLocation(ILocationCallback myLocationCallback) throws NullPointerException, NoLocationAccessException {
        enableMyLocation();
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new NoLocationAccessException();
        }
        LocationProvider.fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LocationProvider.lastLocation = location;
                myLocationCallback.callbackCall(new LatLng(location));
            }
        });
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback myLocationCallback) throws NoLocationAccessException {
        enableMyLocation();
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new NoLocationAccessException();
        }

        LocationRequest locationRequest = LocationRequest.create();
        long millis = (long) (intervalMin * 60 * 1000);
        locationRequest.setInterval(millis);
        locationRequest.setFastestInterval(millis);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
        LocationProvider.fusedLocationProviderClient.requestLocationUpdates(locationRequest, updateLocationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        if (updateLocationCallback != null) {
            LocationProvider.fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
        } else {
            //TODO: new exception
        }
    }
}
