package com.example.geopositionmodule.exceptions;

import com.example.geopositionmodule.LocationProvider;

import java.util.Locale;

import androidx.annotation.Nullable;

public class IntervalValueOutOfRangeException extends Exception{
    @Nullable
    @Override
    public String getMessage() {
        return String.format(Locale.US, "Минимальное допустимое значение интервала = %.2f мин", LocationProvider.MINIMUM_UPDATE_INTERVAL);
    }
}
