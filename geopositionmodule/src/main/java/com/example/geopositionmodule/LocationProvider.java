package com.example.geopositionmodule;

public interface LocationProvider {
//    fun getLastKnownLocation(): LanLng // LatLng - data class с двумя коорданатми
//    fun requestCurrentLocation(callback: (LanLng) -> Unit) // в callback асинхронно должен передаться результат запроса
//    fun requestLocationUpdates(intervalMin: Int, callback: (LanLng) -> Unit)

    Coordinates getLastKnownLocation() throws Exception;
    Coordinates requestCurrentLocation() throws Exception;
    Coordinates requestLocationUpdates(int intervalMin);
}
