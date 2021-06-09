package com.example.geolocationmodule;

/**
 * An interface that describes a callback for location requests
 * Used as an input value for {@link ILocationSupplier} methods
 */
public interface ILocationCallback {
    /**
     * Called on location request success
     *
     * @param location result of a location request
     */
    void callOnSuccess(LatLng location);

    /**
     * Called on location request failure
     *
     * @param e exception from a location request
     */
    void callOnFail(Exception e);
}