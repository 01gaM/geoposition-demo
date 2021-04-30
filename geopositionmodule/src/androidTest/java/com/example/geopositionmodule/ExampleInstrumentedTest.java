package com.example.geopositionmodule;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
   @Test
   public void setAccuracyPriority_GoogleAPI(){
       Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
       LocationProviderClientGoogleAPI locationProviderClient = new LocationProviderClientGoogleAPI(appContext);
       assertEquals(AccuracyPriority.PRIORITY_HIGH_ACCURACY.getCode(), locationProviderClient.getAccuracyPriority()); //default value
       locationProviderClient.setAccuracyPriority(AccuracyPriority.PRIORITY_LOW_POWER);
       assertEquals(AccuracyPriority.PRIORITY_LOW_POWER.getCode(), locationProviderClient.getAccuracyPriority());
   }

    @Test
    public void stopLocationUpdates_noActiveUpdates_GoogleAPI(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationProviderClientGoogleAPI locationProviderClient = new LocationProviderClientGoogleAPI(appContext);
        locationProviderClient.stopLocationUpdates();
        assertNull(locationProviderClient.getUpdateLocationCallback());
    }

    @Test
    public void stopLocationUpdates_noActiveUpdates_AndroidAPI(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationProviderClientAndroidAPI locationProviderClient = new LocationProviderClientAndroidAPI(appContext);
        locationProviderClient.stopLocationUpdates();
        assertNull(locationProviderClient.getUpdateLocationListener());
    }

//    @Test
//    public void stopLocationUpdates_activeUpdate_GoogleAPI(){
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        LocationProviderClientGoogleAPI locationProviderClient = new LocationProviderClientGoogleAPI(appContext);
//        try {
//            locationProviderClient.requestLocationUpdates(1, new ILocationCallback() {
//                @Override
//                public void callOnSuccess(LatLng location) {
//                }
//
//                @Override
//                public void callOnFail(Exception e) {
//                }
//            });
//        } catch (NoLocationAccessException | LocationProviderDisabledException | IntervalValueOutOfRangeException e) {
//            e.printStackTrace();
//        }
//        assertNotNull(locationProviderClient.getUpdateLocationCallback());
//        locationProviderClient.stopLocationUpdates();
//        assertNull(locationProviderClient.getUpdateLocationCallback());
//    }
}