package com.example.geolocationmodule;

import android.content.Context;

import com.example.geolocationmodule.exceptions.AirplaneModeOnException;
import com.example.geolocationmodule.exceptions.DeviceLocationDisabledException;
import com.example.geolocationmodule.exceptions.GooglePlayServicesNotAvailableException;
import com.example.geolocationmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geolocationmodule.exceptions.LocationProviderDisabledException;
import com.example.geolocationmodule.exceptions.LocationPermissionNotGrantedException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class LocationSupplier implements ILocationSupplier {
    private final Context context; //the context in which this LocationProvider instance was created
    private final LocationSupplierClient locationSupplierClient;
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

    public LocationSupplier(Context context) {
        this.context = context;
        this.locationSupplierClient = createLocationProviderClient();
    }

    private LocationSupplierClient createLocationProviderClient() {
        try {
            checkGooglePlayServicesAvailable(context);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            return new LocationSupplierClientAndroidAPI(context);
        }
        return new LocationSupplierClientGoogleAPI(context);
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
    }

    @Override
    public void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException {
        locationSupplierClient.getLastKnownLocation(callback);
    }

    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, AirplaneModeOnException, DeviceLocationDisabledException {
        locationSupplierClient.requestCurrentLocation(callback);
    }

    @Override
    public void cancelCurrentLocationRequest() {
        locationSupplierClient.cancelCurrentLocationRequest();
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException, IntervalValueOutOfRangeException, AirplaneModeOnException, DeviceLocationDisabledException {
        locationSupplierClient.requestLocationUpdates(intervalMin, callback);
    }

    @Override
    public void stopLocationUpdates() {
        locationSupplierClient.stopLocationUpdates();
    }

    @Override
    public void setAccuracyPriority(AccuracyPriority accuracyPriority) {
        locationSupplierClient.setAccuracyPriority(accuracyPriority);
    }
}