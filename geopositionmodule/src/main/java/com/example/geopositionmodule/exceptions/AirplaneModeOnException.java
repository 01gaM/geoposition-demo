package com.example.geopositionmodule.exceptions;

import androidx.annotation.Nullable;

public class AirplaneModeOnException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Невозможно получить данные о местоположении от мобильной сети и сети Wi-Fi при включенном авиа-режиме. " +
                "Попробуйте использовать режим \"Наивысшая точность\" для получения данных от GPS.";
    }
}
