package com.example.geolocationmodule;

import android.location.Location;

import androidx.annotation.NonNull;

/**
 * This class describes model of location data retrieved by location requests
 * Gets data from {@link Location} instance and stores longitude, latitude and speed values only
 */
public class LatLng {
    private final double longitude;
    private final double latitude;
    private final float speed; //meters per second (if there is no speed, value is 0.0)

    public LatLng(double longitude, double latitude, float speed) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
    }

    public LatLng(Location location) {
        this(location.getLongitude(), location.getLatitude(), location.getSpeed());
    }

    @Override
    @NonNull
    public String toString() {
        return "lat=" + latitude +
                ", lon=" + longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
