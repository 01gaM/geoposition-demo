package com.example.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.geopositionmodule.LocationSupplier;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public abstract class BaseCoordinatesActivity extends AppCompatActivity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback {
    protected static boolean isPermissionRequestedFirstTime;
    private final int REQUEST_LOCATION_PERMISSION_CODE = 0;
    protected LocationSupplier locationSupplier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPermissionRequestedFirstTime = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstRun", true);
        locationSupplier = new LocationSupplier(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //Permission Denied!
                handleException(new LocationPermissionNotGrantedException());
            } else {
                resetElementsState();
            }
        }
    }

    protected abstract void resetElementsState();

    protected void requestPermissions() {
        if (isPermissionRequestedFirstTime) {
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstRun", false)
                    .apply();
        }
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_LOCATION_PERMISSION_CODE);
    }

    protected void handleException(Exception e) {
        e.printStackTrace();
        resetElementsState();
        displayAlert(e.getMessage(), "Ошибка!", this);
    }
}
