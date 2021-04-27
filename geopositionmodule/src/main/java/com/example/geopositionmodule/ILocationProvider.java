package com.example.geopositionmodule;

import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

public interface ILocationProvider {
//    fun getLastKnownLocation(): LatLng // LatLng - data class с двумя коорданатми
//    fun requestCurrentLocation(callback: (LatLng) -> Unit) // в callback асинхронно должен передаться результат запроса
//    fun requestLocationUpdates(intervalMin: Int, callback: (LatLng) -> Unit)

    //LatLng getLastKnownLocation() throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException;
    void getLastKnownLocation(ILocationCallback callback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException;
    void requestCurrentLocation(ILocationCallback callback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException;
    void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws NoLocationAccessException, LocationProviderDisabledException, IntervalValueOutOfRangeException;
    void stopLocationUpdates();
}
