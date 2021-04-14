package com.example.geopositionmodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.core.app.ActivityCompat;


public class LocationProvider implements LocationListener, ILocationProvider {
    private Location currLocation;
    private static Location lastLocation = null;
    private Activity activity;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;

    public LocationProvider(Activity activity) throws Exception {
        this.activity = activity;
        this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.locationRequest = LocationRequest.create();
        this.locationRequest.setInterval(100000);
        this.locationRequest.setFastestInterval(100000);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.locationRequest);
        this.locationSettingsRequest = builder.build();

        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currLocation = locationResult.getLastLocation();
                //Coordinates coordinates = new Coordinates(currLocation);
            }
        };

        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest,
                this.locationCallback, Looper.myLooper());
    }

    private void enableMyLocation() throws NoLocationAccessException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        } else {
            throw new NoLocationAccessException();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        currLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {


    }

    @Override
    public void onProviderDisabled(String s) {
        //TODO: check this
        //enableMyLocation();
    }

    @Override
    public LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LocationProvider.lastLocation = location;
                    }
                }
            });
        } else {
            throw new NoLocationAccessException();
        }
        if (LocationProvider.lastLocation != null) {
            return new LatLng(LocationProvider.lastLocation);
        }
        throw new NullPointerException("Последние координаты не были найдены (lastLocation = null).");
    }

    @Override
    public LatLng requestCurrentLocation() throws NullPointerException, NoLocationAccessException {
        enableMyLocation();
        if (currLocation == null) {
            throw new NullPointerException("Текущие координаты не были найдены (currLocation = null).");
        }
        return new LatLng(currLocation);
    }

    @Override
    public LatLng requestLocationUpdates(int intervalMin) {
        return null;
    }
}
