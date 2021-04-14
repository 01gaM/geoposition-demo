package com.example.geopositionmodule;

public interface ILocationProvider {
//    fun getLastKnownLocation(): LatLng // LatLng - data class с двумя коорданатми
//    fun requestCurrentLocation(callback: (LatLng) -> Unit) // в callback асинхронно должен передаться результат запроса
//    fun requestLocationUpdates(intervalMin: Int, callback: (LatLng) -> Unit)

    LatLng getLastKnownLocation() throws Exception;
    LatLng requestCurrentLocation() throws Exception;
    LatLng requestLocationUpdates(int intervalMin);
}
