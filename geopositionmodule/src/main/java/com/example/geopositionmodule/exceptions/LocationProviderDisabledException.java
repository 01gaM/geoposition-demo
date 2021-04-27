package com.example.geopositionmodule.exceptions;

import androidx.annotation.Nullable;

public class LocationProviderDisabledException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Определение местоположения выключено на устройстве.";
    }
}
