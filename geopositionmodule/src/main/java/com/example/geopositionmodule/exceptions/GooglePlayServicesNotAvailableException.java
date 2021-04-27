package com.example.geopositionmodule.exceptions;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class GooglePlayServicesNotAvailableException extends Exception {
    private int connectionResultCode;

    public GooglePlayServicesNotAvailableException(int connectionResultCode) {
        this.connectionResultCode = connectionResultCode;
    }

    @Nullable
    @Override
    public String getMessage() {
        //ConnectionResult connectionResult = new ConnectionResult(connectionResultCode);
        return String.format(Locale.US, "Сервисы Google Play недоступны или необходимо обновление. Код ошибки: %d (%s)", connectionResultCode, getErrorType(connectionResultCode));
    }

    @NonNull
    private String getErrorType(int errorCode) {
        switch (errorCode) {
            case -1:
                return "UNKNOWN";
            case 1:
                return "SERVICE_MISSING";
            case 2:
                return "SERVICE_VERSION_UPDATE_REQUIRED";
            case 3:
                return "SERVICE_DISABLED";
            case 4:
                return "SIGN_IN_REQUIRED";
            case 5:
                return "INVALID_ACCOUNT";
            case 6:
                return "RESOLUTION_REQUIRED";
            case 7:
                return "NETWORK_ERROR";
            case 8:
                return "INTERNAL_ERROR";
            case 9:
                return "SERVICE_INVALID";
            case 10:
                return "DEVELOPER_ERROR";
            case 11:
                return "LICENSE_CHECK_FAILED";
            case 13:
                return "CANCELED";
            case 14:
                return "TIMEOUT";
            case 15:
                return "INTERRUPTED";
            case 16:
                return "API_UNAVAILABLE";
            case 17:
                return "SIGN_IN_FAILED";
            case 18:
                return "SERVICE_UPDATING";
            case 19:
                return "SERVICE_MISSING_PERMISSION";
            case 20:
                return "RESTRICTED_PROFILE";
            case 21:
                return "API_VERSION_UPDATE_REQUIRED";
            case 22:
                return "RESOLUTION_ACTIVITY_NOT_FOUND";
            case 23:
                return "API_DISABLED";
            case 99:
                return "UNFINISHED";
            case 1500:
                return "DRIVE_EXTERNAL_STORAGE_REQUIRED";
            default:
                return (new StringBuilder(31)).append("UNKNOWN_ERROR_CODE(").append(errorCode).append(")").toString();
        }
    }
}
