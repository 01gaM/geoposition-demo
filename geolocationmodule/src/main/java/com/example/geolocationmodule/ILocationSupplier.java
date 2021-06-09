package com.example.geolocationmodule;

import android.Manifest;

import com.example.geolocationmodule.exceptions.AirplaneModeOnException;
import com.example.geolocationmodule.exceptions.DeviceLocationDisabledException;
import com.example.geolocationmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geolocationmodule.exceptions.LocationProviderDisabledException;
import com.example.geolocationmodule.exceptions.LocationPermissionNotGrantedException;
import com.google.android.gms.location.LocationRequest;

/**
 * An interface that describes main functionality of the geolocation module
 */
public interface ILocationSupplier {
    /**
     * Gets last known location from device cache and returns it to callback.
     * This will never activate sensors to compute a new location, and will only ever return a cached location.
     * This method is suitable for applications that do not require an accurate location and that do not want to maintain extra logic for location updates.
     * The method provides the most simple and fast way to get location.
     *
     * @param callback An {@link ILocationCallback} instance used to get either coordinates result as {@link LatLng} or an exception
     * @throws LocationPermissionNotGrantedException if both {@link Manifest.permission#ACCESS_FINE_LOCATION}
     *                                               and {@link Manifest.permission#ACCESS_COARSE_LOCATION} and not granted
     */
    void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException;

    /**
     * Requests current location and returns it to callback.
     * This method may return locations that are a few seconds old, but never returns much older locations.
     * This is suitable for foreground applications that need a single fresh current location.
     *
     * @param callback An {@link ILocationCallback} instance used to get either coordinates result as {@link LatLng} or an exception
     * @throws LocationPermissionNotGrantedException if both {@link Manifest.permission#ACCESS_FINE_LOCATION}
     *                                               and {@link Manifest.permission#ACCESS_COARSE_LOCATION} and not granted
     * @throws LocationProviderDisabledException     if both GPS and network location providers are disabled
     * @throws AirplaneModeOnException               if airplane mode is on and data from network provider is required
     * @throws DeviceLocationDisabledException       if the location tracking is turned of in device settings
     */
    void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException,
            AirplaneModeOnException, DeviceLocationDisabledException;

    /**
     * Cancels current location request initiated by {@link #requestCurrentLocation(ILocationCallback)} method
     */
    void cancelCurrentLocationRequest();

    /**
     * Requests location updates and returns it to callback.
     *
     * @param intervalMin Location update interval value in minutes.
     * @param callback    An {@link ILocationCallback} instance used to get either coordinates result as {@link LatLng} or an exception
     * @throws LocationPermissionNotGrantedException if location permission is not granted
     * @throws LocationProviderDisabledException     if both GPS and network location providers are disabled
     * @throws IntervalValueOutOfRangeException      if input interval value is out of range
     * @throws AirplaneModeOnException               if airplane mode is on and data from network provider is required
     * @throws DeviceLocationDisabledException       if the location tracking is turned of in device settings
     */
    void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException,
            IntervalValueOutOfRangeException, DeviceLocationDisabledException, AirplaneModeOnException;

    /**
     * Stops location updates request initiated by {@link #requestLocationUpdates(double, ILocationCallback)} method
     */
    void stopLocationUpdates();

    /**
     * Sets a specific accuracy priority
     *
     * @param accuracyPriority A new accuracy priority value from {@link com.example.geolocationmodule.AccuracyPriority} enum
     * @see LocationRequest#setPriority(int)
     */
    void setAccuracyPriority(AccuracyPriority accuracyPriority);
}
