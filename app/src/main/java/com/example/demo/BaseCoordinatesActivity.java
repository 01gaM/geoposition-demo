package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import com.example.geopositionmodule.exceptions.NoLocationAccessException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public abstract class BaseCoordinatesActivity extends Activity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback{
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //Permission Denied!
                displayAlert(NoLocationAccessException.message, this);
            }
        }
    }

    protected abstract void resetElementsState();

    protected void requestPermissions() {
        if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME) {
            CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME = false;
        }
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS);
    }

    protected void handleException(Exception e){
        e.printStackTrace();
        resetElementsState();
        displayAlert(e.getMessage(), this);
    }
}
