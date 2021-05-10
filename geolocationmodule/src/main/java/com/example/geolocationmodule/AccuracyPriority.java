package com.example.geolocationmodule;

import com.google.android.gms.location.LocationRequest;

public enum AccuracyPriority {
    PRIORITY_HIGH_ACCURACY(LocationRequest.PRIORITY_HIGH_ACCURACY),
    PRIORITY_BALANCED_POWER_ACCURACY(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
    PRIORITY_LOW_POWER(LocationRequest.PRIORITY_LOW_POWER);

    public final int code;

    AccuracyPriority(int code) {
        this.code = code;
    }
}