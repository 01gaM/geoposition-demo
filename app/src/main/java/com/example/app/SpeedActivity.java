package com.example.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;

public class SpeedActivity extends ServiceBinder {
    private Button showCurrSpeedButton;
    private Button stopSpeedUpdatesButton;
    private TextView tvSpeedValue;
    private ProgressBar progressBar;
    private TextView tvSpeedMessage;
    private final int SPEED_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);
        showCurrSpeedButton = findViewById(R.id.button_request_speed_updates);
        stopSpeedUpdatesButton = findViewById(R.id.button_stop_speed_updates);
        tvSpeedValue = findViewById(R.id.text_current_speed_value);
        progressBar = findViewById(R.id.progress_bar_speed);
        tvSpeedMessage = findViewById(R.id.text_current_speed_message);
        setRequestCode(SPEED_REQUEST_CODE);
        doBindService();

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCurrSpeedButton.setEnabled(false);
                tvSpeedMessage.setVisibility(View.VISIBLE);
                tvSpeedMessage.setText(R.string.request_in_progress);
                progressBar.setVisibility(View.VISIBLE);
                intent.putExtra("showSpeed", true)
                        .putExtra("intervalMin", LocationProvider.MINIMUM_UPDATE_INTERVAL);
                startService(intent);
            }
        };
        showCurrSpeedButton.setOnClickListener(listener);
        stopSpeedUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetElementsState();
                locationProvider.stopLocationUpdates();
                stopService(intent);
                if (updateService != null) {
                    updateService.stopForeground(true);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEED_REQUEST_CODE) {
            Exception e = updateService.getCurrException();
            switch (resultCode) {
                case (UpdateService.UPDATE_SUCCEEDED):
                    LatLng lastUpdatedLocation = updateService.getLocation();
                    tvSpeedMessage.setText(R.string.message_current_speed);
                    progressBar.setVisibility(View.INVISIBLE);
                    tvSpeedValue.setText(String.format("%s м/с", lastUpdatedLocation.getSpeed()));
                    tvSpeedValue.setVisibility(View.VISIBLE);
                    stopSpeedUpdatesButton.setEnabled(true);
                    break;
                case (UpdateService.UPDATE_FAILED):
                    handleException(e);
                    break;
                case (UpdateService.LOCATION_PERMISSION_NOT_GRANTED):
                    if (isPermissionRequestedFirstTime || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions();
                    } else {
                        handleException(e);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (locationProvider != null)
            locationProvider.stopLocationUpdates();
        if (intent != null) {
            stopService(intent);
        }
        doUnbindService();
        super.onDestroy();
    }

    @Override
    protected void resetElementsState() {
        progressBar.setVisibility(View.INVISIBLE);
        tvSpeedValue.setVisibility(View.INVISIBLE);
        tvSpeedMessage.setVisibility(View.INVISIBLE);
        stopSpeedUpdatesButton.setEnabled(false);
        showCurrSpeedButton.setEnabled(true);
    }
}
