package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

public class EmptyLocationCacheException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "В кэше устройства нет данных о местоположении.";
    }
}
