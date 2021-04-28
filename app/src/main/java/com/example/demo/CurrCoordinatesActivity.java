package com.example.demo;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

public class CurrCoordinatesActivity extends BaseCoordinatesActivity {
    private Button showToastButton;
    private LocationProvider locationProvider;
    private ProgressBar progressBar;
    private TextView progressMessage;
    public static final int REQUEST_LOCATION_PERMISSION_SUCCESS = 0;
    static boolean IS_PERMISSION_REQUESTED_FIRST_TIME = true;
    private Button displayMapButton;
    private LatLng currCoordinates = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_coordinates);
        showToastButton = findViewById(R.id.request_curr_coordinates_button);
        progressBar = findViewById(R.id.progressBar);
        progressMessage = findViewById(R.id.request_in_progress_message);
        displayMapButton = findViewById(R.id.button_display_map);
        locationProvider = new LocationProvider(CurrCoordinatesActivity.this);

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToastButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                progressMessage.setVisibility(View.VISIBLE);
                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng coordinates) {
                            currCoordinates = coordinates;
                            Toast toast = Toast.makeText(getApplicationContext(), coordinates.toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show();
                            resetElementsState();
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            handleException(e);
                        }
                    };
                    locationProvider.requestCurrentLocation(myCallback);
                } catch (NoLocationAccessException e) {
                    if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions();
                    } else {
                        handleException(e);
                    }
                } catch (LocationProviderDisabledException | NullPointerException e) {
                    handleException(e);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
        displayMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new MapDialog(CurrCoordinatesActivity.this, currCoordinates);
                dialog.setCancelable(true);
                dialog.show();
            }
        });
    }

    @Override
    protected void resetElementsState() {
        progressBar.setVisibility(View.INVISIBLE);
        progressMessage.setVisibility(View.INVISIBLE);
        showToastButton.setEnabled(true);
    }
}
