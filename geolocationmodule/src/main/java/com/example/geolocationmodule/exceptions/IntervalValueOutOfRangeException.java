package com.example.geolocationmodule.exceptions;

import com.example.geolocationmodule.LocationSupplier;

import java.util.Locale;

import androidx.annotation.Nullable;

public class IntervalValueOutOfRangeException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return String.format(Locale.US, "Значение интервала должно быть от %.3f до %.0f мин",
                LocationSupplier.MINIMUM_UPDATE_INTERVAL,
                LocationSupplier.MAXIMUM_UPDATE_INTERVAL);
    }
}
