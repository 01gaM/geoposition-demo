package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

public class DeviceLocationDisabledException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Определение местоположения с помощью GPS, Wi-Fi и мобильных сетей выключено в настройках устройства.";
    }
}
