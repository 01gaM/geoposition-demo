package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.geopositionmodule.GooglePlayServicesNotAvailableException;
import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.LocationProviderDisabledException;
import com.example.geopositionmodule.NoLocationAccessException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class SpeedActivity extends Activity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback {
    private Button showCurrSpeedButton;
    private Button stopSpeedUpdatesButton;
    private TextView tvSpeedValue;
    private TextView waitingMessage;
    private ProgressBar progressBar;
    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);
        showCurrSpeedButton = findViewById(R.id.button_request_speed_updates);
        stopSpeedUpdatesButton = findViewById(R.id.button_stop_speed_updates);
        waitingMessage = findViewById(R.id.text_waiting_message);
        tvSpeedValue = findViewById(R.id.text_current_speed_value);
        progressBar = findViewById(R.id.progress_bar_speed);

        try {
            locationProvider = new LocationProvider(SpeedActivity.this);
        } catch (GooglePlayServicesNotAvailableException e) {
            displayAlert(e.getMessage(), SpeedActivity.this, true);
            e.printStackTrace();
        }

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCurrSpeedButton.setEnabled(false);
                locationProvider.stopLocationUpdates();
                waitingMessage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng lastUpdatedLocation) {
                            waitingMessage.setVisibility(View.INVISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            tvSpeedValue.setText(lastUpdatedLocation.getSpeed() + " м/с");
                            tvSpeedValue.setVisibility(View.VISIBLE);
                            stopSpeedUpdatesButton.setEnabled(true);
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            e.printStackTrace();
                            waitingMessage.setVisibility(View.INVISIBLE);
                            displayAlert(e.getMessage(), SpeedActivity.this, false);
                        }
                    };
                    locationProvider.requestLocationUpdates(LocationProvider.MINIMUM_UPDATE_INTERVAL, myCallback);
                } catch (NoLocationAccessException e) {
                    if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME) {
                            CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME = false;
                        }
                        ActivityCompat.requestPermissions(SpeedActivity.this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS);
                    } else {
                        e.printStackTrace();
                        displayAlert(e.getMessage(), SpeedActivity.this, false);
                        resetElementsState();
                    }
                } catch (LocationProviderDisabledException | IntervalValueOutOfRangeException e) {
                    e.printStackTrace();
                    displayAlert(e.getMessage(), SpeedActivity.this, false);
                    resetElementsState();
                }
            }
        };
        showCurrSpeedButton.setOnClickListener(listener);

        stopSpeedUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetElementsState();
                locationProvider.stopLocationUpdates();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (locationProvider != null)
            locationProvider.stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //Permission Denied!
                displayAlert(NoLocationAccessException.message, SpeedActivity.this, false);
            }
        }
    }

    private void resetElementsState(){
        progressBar.setVisibility(View.INVISIBLE);
        waitingMessage.setVisibility(View.INVISIBLE);
        tvSpeedValue.setVisibility(View.INVISIBLE);
        stopSpeedUpdatesButton.setEnabled(false);
        showCurrSpeedButton.setEnabled(true);
    }
}
