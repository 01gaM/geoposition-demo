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

/**
 * An instance of this class should be used to call functions of {@link ILocationSupplier} interface
 * This is the only instance that user has to create to use geolocation module functionality
 */
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
        this.locationSupplierClient = createLocationSupplierClient();
    }

    /**
     * Checks Google Play services availability and creates a new LocationSupplierClient instance.
     *
     * @return If Google Play services are available and up-to-date then a {@link LocationSupplierClientGoogleAPI} instance will be returned,
     * else a {@link LocationSupplierClientAndroidAPI} will be returned.
     */
    private LocationSupplierClient createLocationSupplierClient() {
        try {
            checkGooglePlayServicesAvailable(context);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            return new LocationSupplierClientAndroidAPI(context);
        }
        return new LocationSupplierClientGoogleAPI(context);
    }

    /**
     * Checks whether the Google Play services APK is available and up-to-date on this device or not
     *
     * @param context a context in which Google Play services availability is being checked. Used for {@link GoogleApiAvailability#isGooglePlayServicesAvailable(Context)} method
     * @throws GooglePlayServicesNotAvailableException if the Google Play services APK is not available or it's version is out-of-date on this device
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
