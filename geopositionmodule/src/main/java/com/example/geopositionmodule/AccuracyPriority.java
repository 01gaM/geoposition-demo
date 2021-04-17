package com.example.geopositionmodule;

public enum AccuracyPriority {
    PRIORITY_HIGH_ACCURACY(100),
    PRIORITY_BALANCED_POWER_ACCURACY(102),
    PRIORITY_LOW_POWER(104),
    PRIORITY_NO_POWER(105);

    public final int code;

    AccuracyPriority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}