package com.example.demo;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

public class SpeedActivity extends BaseCoordinatesActivity {
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
        locationProvider = new LocationProvider(SpeedActivity.this);
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
                            tvSpeedValue.setText(String.format("%s м/с", lastUpdatedLocation.getSpeed()));
                            tvSpeedValue.setVisibility(View.VISIBLE);
                            stopSpeedUpdatesButton.setEnabled(true);
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            handleException(e);
                        }
                    };
                    locationProvider.requestLocationUpdates(LocationProvider.MINIMUM_UPDATE_INTERVAL, myCallback);
                } catch (NoLocationAccessException e) {
                    if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions();
                    } else {
                        handleException(e);
                    }
                } catch (LocationProviderDisabledException | IntervalValueOutOfRangeException e) {
                    handleException(e);
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
    protected void resetElementsState() {
        progressBar.setVisibility(View.INVISIBLE);
        waitingMessage.setVisibility(View.INVISIBLE);
        tvSpeedValue.setVisibility(View.INVISIBLE);
        stopSpeedUpdatesButton.setEnabled(false);
        showCurrSpeedButton.setEnabled(true);
    }
}
