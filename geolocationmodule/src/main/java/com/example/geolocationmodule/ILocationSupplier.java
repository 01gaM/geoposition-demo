package com.example.geolocationmodule;

import com.example.geolocationmodule.exceptions.AirplaneModeOnException;
import com.example.geolocationmodule.exceptions.DeviceLocationDisabledException;
import com.example.geolocationmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geolocationmodule.exceptions.LocationProviderDisabledException;
import com.example.geolocationmodule.exceptions.LocationPermissionNotGrantedException;

public interface ILocationSupplier {
    void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException;

    void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException,
            AirplaneModeOnException, DeviceLocationDisabledException;

    void cancelCurrentLocationRequest();

    void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException,
            IntervalValueOutOfRangeException, DeviceLocationDisabledException, AirplaneModeOnException;

    void stopLocationUpdates();

    void setAccuracyPriority(AccuracyPriority accuracyPriority);
}
