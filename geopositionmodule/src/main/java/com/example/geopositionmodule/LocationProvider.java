package com.example.geopositionmodule;

import android.content.Context;

import com.example.geopositionmodule.exceptions.GooglePlayServicesNotAvailableException;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class LocationProvider implements ILocationProvider {
    private final Context context; //the context in which this LocationProvider instance was created
    private final LocationProviderClient locationProviderClient;
    /**
     * The minimum location update interval in minutes (equals 0.05 minutes = 3 seconds)
     * Used for {@link #requestLocationUpdates(double, ILocationCallback)} method
     */
    public static final double MINIMUM_UPDATE_INTERVAL = 0.05;

    public LocationProvider(Context context) {
        this.context = context;
        this.locationProviderClient = getLocationProviderClient();
    }

    private LocationProviderClient getLocationProviderClient() {
//        try {
//            checkGooglePlayServicesAvailable();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//            return new LocationProviderLM(context);
//        }
//        return new LocationProviderF(context);
        return new LocationProviderClientAndroidAPI(context);
    }

    /**
     * This method checks whether the Google Play services APK is available and up-to-date on this device or not.
     *
     * @throws GooglePlayServicesNotAvailableException Exception is thrown when the Google Play services APK is not available or it's version is out-of-date on this device.
     */
    private void checkGooglePlayServicesAvailable() throws GooglePlayServicesNotAvailableException {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            //Google Play Services is missing or update is required
            throw new GooglePlayServicesNotAvailableException(resultCode);
        }
    }

    @Override
    public void getLastKnownLocation(ILocationCallback callback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        locationProviderClient.getLastKnownLocation(callback);
    }

    @Override
    public void requestCurrentLocation(ILocationCallback callback) throws NullPointerException, NoLocationAccessException, LocationProviderDisabledException {
        locationProviderClient.requestCurrentLocation(callback);
    }

    @Override
    public void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws NoLocationAccessException, LocationProviderDisabledException, IntervalValueOutOfRangeException {
        locationProviderClient.requestLocationUpdates(intervalMin, callback);
    }

    @Override
    public void stopLocationUpdates() {
        locationProviderClient.stopLocationUpdates();
    }
}
