package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

public class AirplaneModeOnException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Невозможно получить данные о местоположении от мобильной сети и сети Wi-Fi при включенном авиа-режиме. " +
                "Попробуйте использовать другой режим определения местоположения или выключите авиа-режим.";
    }
}
