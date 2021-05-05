package com.example.geopositionmodule;

import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.EmptyLocationCacheException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

public interface ILocationProvider {
    void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException;
    void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, AirplaneModeOnException;
    void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, IntervalValueOutOfRangeException, AirplaneModeOnException;
    void stopLocationUpdates();
    void setAccuracyPriority(AccuracyPriority accuracyPriority);
}
