package com.example.geopositionmodule;

import android.location.Location;

public class LatLng {
    private double longitude;
    private double latitude;

    public LatLng(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public LatLng(Location location) {
        this(location.getLongitude(), location.getLatitude());
    }

    @Override
    public String toString() {
        return "lon=" + longitude +
                ", lat=" + latitude;
    }
}
