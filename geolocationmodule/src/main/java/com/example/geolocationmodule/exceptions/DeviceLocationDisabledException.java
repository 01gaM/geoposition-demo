package com.example.geolocationmodule.exceptions;

import android.Manifest;

import androidx.annotation.Nullable;

/**
 * Thrown if both {@link Manifest.permission#ACCESS_FINE_LOCATION}
 * and {@link Manifest.permission#ACCESS_COARSE_LOCATION} and not granted
 */
public class DeviceLocationDisabledException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Определение местоположения с помощью GPS, Wi-Fi и мобильных сетей выключено в настройках устройства.";
    }
}
