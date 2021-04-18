package com.example.geopositionmodule;

public interface ILocationCallback {
    void callOnSuccess(LatLng location);
    void callOnFail(Exception e);
}