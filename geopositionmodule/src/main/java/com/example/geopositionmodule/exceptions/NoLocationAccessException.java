package com.example.geopositionmodule.exceptions;

import androidx.annotation.Nullable;

public class NoLocationAccessException extends Exception {
    public static final String message = "Нет разрешения на доступ к геопозиции.";
    @Nullable
    @Override
    public String getMessage() {
        return NoLocationAccessException.message;
    }
}
