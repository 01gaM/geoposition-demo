package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

/**
 * Thrown if location permission is not granted in the device settings
 */
public class LocationPermissionNotGrantedException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Нет разрешения на доступ к геолокации.";
    }
}
