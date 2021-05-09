package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

public class LocationProviderDisabledException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Не удалось установить соединение с источником данных о местоположении. Попробуйте выбрать другой режим или повторите запрос позже.";
    }
}
