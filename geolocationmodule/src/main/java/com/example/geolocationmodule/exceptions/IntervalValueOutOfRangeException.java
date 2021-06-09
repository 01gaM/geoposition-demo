package com.example.geolocationmodule.exceptions;

import android.location.LocationManager;

import com.example.geolocationmodule.ILocationCallback;
import com.example.geolocationmodule.LocationSupplier;

import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * Thrown if input interval value for {@link com.example.geolocationmodule.ILocationSupplier#requestLocationUpdates(double, ILocationCallback)}
 * is out of range  when using network provider data by {@link LocationManager}
 */
public class IntervalValueOutOfRangeException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return String.format(Locale.US, "Значение интервала должно быть от %.3f до %.0f мин",
                LocationSupplier.MINIMUM_UPDATE_INTERVAL,
                LocationSupplier.MAXIMUM_UPDATE_INTERVAL);
    }
}
