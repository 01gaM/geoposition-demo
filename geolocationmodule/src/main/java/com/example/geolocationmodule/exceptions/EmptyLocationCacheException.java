package com.example.geolocationmodule.exceptions;

import androidx.annotation.Nullable;

/**
 * Thrown when null last location found due to empty device location cache:
 * - Location is turned off in the device settings (disabling location clears the cache)
 * - The device never recorded its location (a new device or a device that has been restored to factory settings)
 * - Google Play services on the device has restarted, and there is no active Fused Location Provider client that has requested location.
 */
public class EmptyLocationCacheException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "В кэше устройства нет данных о местоположении.";
    }
}
