package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.GooglePlayServicesNotAvailableException;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.NoLocationAccessException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class LastCoordinatesActivity extends Activity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback {
    private Button showToastButton;
    private LocationProvider locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_coordinates);
        showToastButton = findViewById(R.id.request_last_coordinates_button);
        try {
            locationReceiver = new LocationProvider(LastCoordinatesActivity.this);
        } catch (GooglePlayServicesNotAvailableException e) {
            displayAlert(e.getMessage(), LastCoordinatesActivity.this, true);
            e.printStackTrace();
        }

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LatLng lastCoordinates = locationReceiver.getLastKnownLocation();
                    Toast toast = Toast.makeText(getApplicationContext(), lastCoordinates.toString(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 400);
                    toast.show();
                } catch (NoLocationAccessException e) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        ActivityCompat.requestPermissions(LastCoordinatesActivity.this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS);
                    } else {
                        e.printStackTrace();
                        displayAlert(e.getMessage(), LastCoordinatesActivity.this, false);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    displayAlert(e.getMessage(), LastCoordinatesActivity.this, false);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //Permission Denied!
                displayAlert(NoLocationAccessException.message, LastCoordinatesActivity.this, false);
            }
        }
    }

}
