package com.example.demo;

import android.Manifest;
import android.content.pm.PackageManager;

import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public abstract class BaseCoordinatesActivity extends AppCompatActivity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback{
    protected static boolean isPermissionRequestedFirstTime = true;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_CODE) {
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
            isPermissionRequestedFirstTime = false;
        }
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_CODE);
    }

    protected void handleException(Exception e){
        e.printStackTrace();
        resetElementsState();
        displayAlert(e.getMessage(), "Ошибка!",this);
    }
}
