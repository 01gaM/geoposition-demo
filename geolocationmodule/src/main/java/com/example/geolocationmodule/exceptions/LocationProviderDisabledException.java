package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

/**
 * Thrown if both GPS and network location providers are disabled (can't establish connection with location provider within 1 minute)
 */
public class LocationProviderDisabledException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Не удалось установить соединение с источником данных о местоположении. Попробуйте выбрать другой режим или повторите запрос позже.";
    }
}
