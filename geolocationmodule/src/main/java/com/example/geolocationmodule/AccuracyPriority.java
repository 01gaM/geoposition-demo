package com.example.geolocationmodule;

import com.google.android.gms.location.LocationRequest;

/**
 * The priority of the request is a strong hint to the LocationClient for which location sources to use.
 * {@link #PRIORITY_HIGH_ACCURACY} is more likely to use GPS (accurate as possible at the expense of battery life)
 * {@link #PRIORITY_BALANCED_POWER_ACCURACY} is more likely to use WIFI & Cell tower positioning (~100m "block" accuracy)
 * {@link #PRIORITY_LOW_POWER} is more likely to use Cell tower positioning (~10km "city" accuracy)
 *
 * @see LocationRequest#setPriority(int)
 */
public enum AccuracyPriority {
    PRIORITY_HIGH_ACCURACY(LocationRequest.PRIORITY_HIGH_ACCURACY),
    PRIORITY_BALANCED_POWER_ACCURACY(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
    PRIORITY_LOW_POWER(LocationRequest.PRIORITY_LOW_POWER);

    public final int code;

    AccuracyPriority(int code) {
        this.code = code;
    }
}