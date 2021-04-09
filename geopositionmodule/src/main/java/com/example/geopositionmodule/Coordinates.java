package com.example.geopositionmodule;

import android.location.Location;

public class Coordinates {
    private double longitude;
    private double latitude;

    public Coordinates(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Coordinates(Location location){
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return  "lon=" + longitude +
                ", lat=" + latitude;
    }
}
