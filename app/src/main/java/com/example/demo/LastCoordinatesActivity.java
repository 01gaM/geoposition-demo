package com.example.demo;

import android.Manifest;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

public class LastCoordinatesActivity extends BaseCoordinatesActivity {
    private Button showToastButton;
    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_coordinates);
        showToastButton = findViewById(R.id.request_last_coordinates_button);
        locationProvider = new LocationProvider(LastCoordinatesActivity.this);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng lastCoordinates) {
                            Toast toast = Toast.makeText(getApplicationContext(), lastCoordinates.toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show();
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            handleException(e);
                        }
                    };
                    locationProvider.getLastKnownLocation(myCallback);
                } catch (NoLocationAccessException e) {
                    if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions();
                    } else {
                        handleException(e);
                    }
                } catch (NullPointerException | LocationProviderDisabledException e) {
                    handleException(e);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
    }

    @Override
    protected void resetElementsState() {
    }
}
