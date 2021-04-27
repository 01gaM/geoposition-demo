package com.example.geopositionmodule;

import android.location.Location;

public class LatLng {
    private double longitude;
    private double latitude;
    private float speed; //if there is no speed, value is 0.0

    public LatLng(double longitude, double latitude, float speed) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
    }

    public LatLng(Location location) {
        this(location.getLongitude(), location.getLatitude(), location.getSpeed());
    }

    @Override
    public String toString() {
        return "lat=" + latitude +
                ", lon=" + longitude;
    }

    public float getSpeed() {
        return speed;
    }
}
