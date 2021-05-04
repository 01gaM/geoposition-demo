package com.example.geopositionmodule;

import com.example.geopositionmodule.exceptions.EmptyLocationCacheException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

public interface ILocationProvider {
//    fun getLastKnownLocation(): LatLng // LatLng - data class с двумя коорданатми
//    fun requestCurrentLocation(callback: (LatLng) -> Unit) // в callback асинхронно должен передаться результат запроса
//    fun requestLocationUpdates(intervalMin: Int, callback: (LatLng) -> Unit)

    void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException;
    void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException;
    void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, IntervalValueOutOfRangeException;
    void stopLocationUpdates();
    void setAccuracyPriority(AccuracyPriority accuracyPriority);
}
