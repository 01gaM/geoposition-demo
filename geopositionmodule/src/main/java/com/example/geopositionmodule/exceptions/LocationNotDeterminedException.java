package com.example.geopositionmodule.exceptions;

import androidx.annotation.Nullable;

public class LocationNotDeterminedException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Координаты не были определены.";
    }
}
