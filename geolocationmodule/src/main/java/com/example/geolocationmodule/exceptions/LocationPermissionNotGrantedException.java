package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

public class LocationPermissionNotGrantedException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Нет разрешения на доступ к геолокации.";
    }
}
