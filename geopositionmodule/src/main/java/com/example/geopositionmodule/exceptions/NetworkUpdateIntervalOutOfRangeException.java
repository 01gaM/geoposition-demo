package com.example.geopositionmodule.exceptions;

import com.example.geopositionmodule.LocationProviderClientAndroidAPI;

import java.util.Locale;

import androidx.annotation.Nullable;

public class NetworkUpdateIntervalOutOfRangeException extends IntervalValueOutOfRangeException {
    @Nullable
    @Override
    public String getMessage() {
        return String.format(Locale.US, "Значение интервала для получения данных от сети должно быть больше %.2f мин. " +
                        "Для использования меньшего интервала требуется GPS. Попробуйте режим \"Наивысшая точность\".",
                LocationProviderClientAndroidAPI.MINIMUM_UPDATE_INTERVAL_NETWORK);
    }
}
