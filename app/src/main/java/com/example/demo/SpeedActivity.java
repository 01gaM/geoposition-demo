package com.example.demo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;

public class SpeedActivity extends BaseCoordinatesActivity {
    private Button showCurrSpeedButton;
    private Button stopSpeedUpdatesButton;
    private TextView tvSpeedValue;
    private TextView waitingMessage;
    private ProgressBar progressBar;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);
        showCurrSpeedButton = findViewById(R.id.button_request_speed_updates);
        stopSpeedUpdatesButton = findViewById(R.id.button_stop_speed_updates);
        waitingMessage = findViewById(R.id.text_waiting_message);
        tvSpeedValue = findViewById(R.id.text_current_speed_value);
        progressBar = findViewById(R.id.progress_bar_speed);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCurrSpeedButton.setEnabled(false);
                waitingMessage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                int TASK_CODE = 2;
                PendingIntent pendingIntent = createPendingResult(TASK_CODE, new Intent(), 0);
                intent = new Intent(getApplicationContext(), UpdateService.class)
                        .putExtra("pendingIntent", pendingIntent)
                        .putExtra("intervalMin", LocationProvider.MINIMUM_UPDATE_INTERVAL)
                        .putExtra("showSpeed", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startService(intent);
            }
        };
        showCurrSpeedButton.setOnClickListener(listener);
        stopSpeedUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetElementsState();
                LocationProvider locationProvider = UpdateService.getLocationProvider();
                locationProvider.stopLocationUpdates();
                stopService(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Exception e = UpdateService.getCurrException();
        switch (resultCode) {
            case (UpdateService.UPDATE_SUCCEEDED):
                LatLng lastUpdatedLocation = UpdateService.getLocation();
                waitingMessage.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onDestroy() {
        LocationProvider locationProvider = UpdateService.getLocationProvider();
        if (locationProvider != null)
            locationProvider.stopLocationUpdates();
        if (intent != null) {
            stopService(intent);
        }
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
