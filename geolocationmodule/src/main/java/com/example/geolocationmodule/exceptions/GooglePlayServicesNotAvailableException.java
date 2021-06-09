package com.example.geolocationmodule.exceptions;

import com.google.android.gms.common.ConnectionResult;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Thrown if the Google Play services APK is not available or it's version is out-of-date on this device
 * Minimum version of Google Play services supported by Fused Location Provider API is 11.0
 */
public class GooglePlayServicesNotAvailableException extends Exception {
    private final int connectionResultCode;

    public GooglePlayServicesNotAvailableException(int connectionResultCode) {
        this.connectionResultCode = connectionResultCode;
    }

    @Nullable
    @Override
    public String getMessage() {
        return String.format(Locale.US, "Сервисы Google Play недоступны или необходимо обновление. Код ошибки: %d (%s)", connectionResultCode, getErrorType(connectionResultCode));
    }

    @NonNull
    private String getErrorType(int errorCode) {
        switch (errorCode) {
            case ConnectionResult.UNKNOWN:
                return "UNKNOWN";
            case ConnectionResult.SERVICE_MISSING:
                return "SERVICE_MISSING";
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                return "SERVICE_VERSION_UPDATE_REQUIRED";
            case ConnectionResult.SERVICE_DISABLED:
                return "SERVICE_DISABLED";
            case ConnectionResult.SIGN_IN_REQUIRED:
                return "SIGN_IN_REQUIRED";
            case ConnectionResult.INVALID_ACCOUNT:
                return "INVALID_ACCOUNT";
            case ConnectionResult.RESOLUTION_REQUIRED:
                return "RESOLUTION_REQUIRED";
            case ConnectionResult.NETWORK_ERROR:
                return "NETWORK_ERROR";
            case ConnectionResult.INTERNAL_ERROR:
                return "INTERNAL_ERROR";
            case ConnectionResult.SERVICE_INVALID:
                return "SERVICE_INVALID";
            case ConnectionResult.DEVELOPER_ERROR:
                return "DEVELOPER_ERROR";
            case ConnectionResult.LICENSE_CHECK_FAILED:
                return "LICENSE_CHECK_FAILED";
            case ConnectionResult.CANCELED:
                return "CANCELED";
            case ConnectionResult.TIMEOUT:
                return "TIMEOUT";
            case ConnectionResult.INTERRUPTED:
                return "INTERRUPTED";
            case ConnectionResult.API_UNAVAILABLE:
                return "API_UNAVAILABLE";
            case ConnectionResult.SIGN_IN_FAILED:
                return "SIGN_IN_FAILED";
            case ConnectionResult.SERVICE_UPDATING:
                return "SERVICE_UPDATING";
            case ConnectionResult.SERVICE_MISSING_PERMISSION:
                return "SERVICE_MISSING_PERMISSION";
            case ConnectionResult.RESTRICTED_PROFILE:
                return "RESTRICTED_PROFILE";
            case 21:
                return "API_VERSION_UPDATE_REQUIRED";
            case ConnectionResult.RESOLUTION_ACTIVITY_NOT_FOUND:
                return "RESOLUTION_ACTIVITY_NOT_FOUND";
            case ConnectionResult.API_DISABLED:
                return "API_DISABLED";
            case 99:
                return "UNFINISHED";
            case ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED:
                return "DRIVE_EXTERNAL_STORAGE_REQUIRED";
            default:
                return "UNKNOWN_ERROR_CODE(" + errorCode + ")";
        }
    }
}
