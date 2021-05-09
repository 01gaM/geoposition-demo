package com.example.geolocationmodule;

public interface ILocationCallback {
    void callOnSuccess(LatLng location);
    void callOnFail(Exception e);
}