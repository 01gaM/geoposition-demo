package com.example.geopositionmodule;

import android.content.Context;

import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.DeviceLocationDisabledException;
import com.example.geopositionmodule.exceptions.EmptyLocationCacheException;
import com.example.geopositionmodule.exceptions.GooglePlayServicesNotAvailableException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class LocationProvider implements ILocationProvider {
    private final Context context; //the context in which this LocationProvider instance was created
    private final LocationProviderClient locationProviderClient;
    /**
     * The minimum location update interval in minutes (equals 0.016 minutes ~ 1 second)
     * Used for {@link #requestLocationUpdates(double, ILocationCallback)} method
     */
    public static final double MINIMUM_UPDATE_INTERVAL = 0.016;
    /**
     * The maximum location update interval in minutes (equals 45000 minutes ~ 1 month)
     * Used for {@link #requestLocationUpdates(double, ILocationCallback)} method
     */
    public static final double MAXIMUM_UPDATE_INTERVAL = 45000;

    public LocationProvider(Context context) {
        this.context = context;
        this.locationProviderClient = createLocationProviderClient();
    }

    private LocationProviderClient createLocationProviderClient() {
        try {
            checkGooglePlayServicesAvailable(context);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            return new LocationProviderClientAndroidAPI(context);
        }
        return new LocationProviderClientGoogleAPI(context);
      //  return new LocationProviderClientAndroidAPI(context);
    }

    /**
     * This method checks whether the Google Play services APK is available and up-to-date on this device or not.
     *
     * @throws GooglePlayServicesNotAvailableException Exception is thrown when the Google Play services APK is not available or it's version is out-of-date on this device.
     */
    public static void checkGooglePlayServicesAvailable(Context context) throws GooglePlayServicesNotAvailableException {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            //Google Play Services is missing or update is required
            throw new GooglePlayServicesNotAvailableException(resultCode);
        }
        //throw new GooglePlayServicesNotAvailableException(2);
    }

    @Override
    public void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException {
        locationProviderClient.getLastKnownLocation(callback);
    }

    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, AirplaneModeOnException, DeviceLocationDisabledException {
        locationProviderClient.requestCurrentLocation(callback);
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, IntervalValueOutOfRangeException, AirplaneModeOnException, DeviceLocationDisabledException {
        locationProviderClient.requestLocationUpdates(intervalMin, callback);
    }

    @Override
    public void stopLocationUpdates() {
        locationProviderClient.stopLocationUpdates();
    }

    @Override
    public void setAccuracyPriority(AccuracyPriority accuracyPriority) {
        locationProviderClient.setAccuracyPriority(accuracyPriority);
    }
}
