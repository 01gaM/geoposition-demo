package com.example.geopositionmodule;

import androidx.annotation.Nullable;

public class NoLocationAccessException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Нет разрешения на доступ к геопозиции.";
    }
}
