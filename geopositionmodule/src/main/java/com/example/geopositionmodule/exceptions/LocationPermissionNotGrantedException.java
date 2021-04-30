package com.example.geopositionmodule.exceptions;

import androidx.annotation.Nullable;

public class LocationPermissionNotGrantedException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Нет разрешения на доступ к геопозиции.";
    }
}
