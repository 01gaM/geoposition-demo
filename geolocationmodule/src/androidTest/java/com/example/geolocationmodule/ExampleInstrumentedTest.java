package com.example.geolocationmodule;

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
       LocationSupplierClientGoogleAPI locationProviderClient = new LocationSupplierClientGoogleAPI(appContext);
       assertEquals(AccuracyPriority.PRIORITY_HIGH_ACCURACY.code, locationProviderClient.getAccuracyPriority().code); //default value
       locationProviderClient.setAccuracyPriority(AccuracyPriority.PRIORITY_LOW_POWER);
       assertEquals(AccuracyPriority.PRIORITY_LOW_POWER.code, locationProviderClient.getAccuracyPriority().code);
   }

    @Test
    public void stopLocationUpdates_noActiveUpdates_GoogleAPI(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationSupplierClientGoogleAPI locationProviderClient = new LocationSupplierClientGoogleAPI(appContext);
        locationProviderClient.stopLocationUpdates();
        assertNull(locationProviderClient.getUpdateLocationCallback());
    }

    @Test
    public void stopLocationUpdates_noActiveUpdates_AndroidAPI(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationSupplierClientAndroidAPI locationProviderClient = new LocationSupplierClientAndroidAPI(appContext);
        locationProviderClient.stopLocationUpdates();
        assertNull(locationProviderClient.getUpdateLocationListener());
    }
}