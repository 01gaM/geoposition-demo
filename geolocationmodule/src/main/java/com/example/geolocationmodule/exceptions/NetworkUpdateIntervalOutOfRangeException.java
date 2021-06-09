package com.example.geolocationmodule.exceptions;

import com.example.geolocationmodule.ILocationCallback;
import com.example.geolocationmodule.LocationSupplierClientAndroidAPI;

import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * Thrown if input interval value for {@link com.example.geolocationmodule.ILocationSupplier#requestLocationUpdates(double, ILocationCallback)} is out of range
 */
public class NetworkUpdateIntervalOutOfRangeException extends IntervalValueOutOfRangeException {
    @Nullable
    @Override
    public String getMessage() {
        return String.format(Locale.US, "Значение интервала для получения данных от сети должно быть больше %.2f мин. " +
                        "Для использования меньшего интервала требуется GPS. Попробуйте режим \"Наивысшая точность\".",
                LocationSupplierClientAndroidAPI.MINIMUM_UPDATE_INTERVAL_NETWORK);
    }
}
