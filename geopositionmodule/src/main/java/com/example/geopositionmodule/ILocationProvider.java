package com.example.geopositionmodule;

public interface ILocationProvider {
//    fun getLastKnownLocation(): LatLng // LatLng - data class с двумя коорданатми
//    fun requestCurrentLocation(callback: (LatLng) -> Unit) // в callback асинхронно должен передаться результат запроса
//    fun requestLocationUpdates(intervalMin: Int, callback: (LatLng) -> Unit)

    LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException;
    void requestCurrentLocation(ILocationCallback callback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException;
    //TODO: change intervalMin type to int
    void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws NoLocationAccessException, LocationProviderDisabledException, IntervalValueOutOfRangeException;
}
