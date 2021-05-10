package com.example.app;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geolocationmodule.ILocationCallback;
import com.example.geolocationmodule.LatLng;
import com.example.geolocationmodule.LocationSupplier;
import com.example.geolocationmodule.exceptions.LocationPermissionNotGrantedException;

public class LastCoordinatesActivity extends BaseCoordinatesActivity {
    private Button getLastLocationButton;
    private Button displayMapButton;
    private LatLng lastCoordinates = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_coordinates);
        getLastLocationButton = findViewById(R.id.request_last_coordinates_button);
        locationSupplier = new LocationSupplier(LastCoordinatesActivity.this);
        displayMapButton = findViewById(R.id.button_display_map);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng coordinates) {
                            Toast toast = Toast.makeText(getApplicationContext(), coordinates.toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show();
                            lastCoordinates = coordinates;
                            displayMapButton.setEnabled(true);
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            handleException(e);
                        }
                    };
                    locationSupplier.getLastKnownLocation(myCallback);
                } catch (LocationPermissionNotGrantedException e) {
                    if (isPermissionRequestedFirstTime || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions();
                    } else {
                        handleException(e);
                    }
                }
            }
        };
        getLastLocationButton.setOnClickListener(listener);
        displayMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new MapDialog(LastCoordinatesActivity.this, lastCoordinates);
                dialog.setCancelable(true);
                dialog.show();
            }
        });
    }

    @Override
    protected void resetElementsState() {
    }
}
