package com.example.demo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;

import java.util.concurrent.TimeUnit;

public class UpdateCoordinatesActivity extends BaseCoordinatesActivity {
    private Button requestUpdatesButton;
    private Button stopUpdatesButton;
    private EditText editDelay;
    private TextView tvTimer;
    private CountDownTimer cTimer = null;
    private TextView waitingMessage;
    private Button displayMapButton;
    private LatLng currCoordinates = null;
    private MapDialog mapDialog;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_coordinates);
        requestUpdatesButton = findViewById(R.id.button_request_updates);
        stopUpdatesButton = findViewById(R.id.button_stop_updates);
        tvTimer = findViewById(R.id.text_timer);
        waitingMessage = findViewById(R.id.text_waiting_message);
        editDelay = findViewById(R.id.edit_text_interval);
        displayMapButton = findViewById(R.id.button_display_map);
        editDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                requestUpdatesButton.setEnabled(!stopUpdatesButton.isEnabled() && !editDelay.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDelay.setEnabled(false);
                requestUpdatesButton.setEnabled(false);
                stopUpdatesButton.setEnabled(true);
                if (cTimer != null)
                    cTimer.cancel();
                double minutes = Double.parseDouble(editDelay.getText().toString());
                int TASK_CODE = 1;
                PendingIntent pendingIntent = createPendingResult(TASK_CODE, new Intent(), 0);
                intent = new Intent(getApplicationContext(), UpdateService.class)
                        .putExtra("pendingIntent", pendingIntent)
                        .putExtra("intervalMin", minutes);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startService(intent);
                startTimer(minutes);
            }
        };
        requestUpdatesButton.setOnClickListener(listener);
        stopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                resetElementsState();
                stopService(intent);
            }
        });
        displayMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapDialog = new MapDialog(UpdateCoordinatesActivity.this, currCoordinates);
                mapDialog.setCancelable(true);
                mapDialog.show();
                mapDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mapDialog = null;
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Exception e = UpdateService.getCurrException();
        switch (resultCode) {
            case (0):
                LatLng lastUpdatedLocation = UpdateService.getLocation();
                Toast toast = Toast.makeText(UpdateCoordinatesActivity.this, lastUpdatedLocation.toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 400);
                toast.show();
                cTimer.start(); //restart timer
                displayMapButton.setEnabled(true);
                currCoordinates = lastUpdatedLocation;
                if (mapDialog != null) {
                    mapDialog.setCurrentPoint(lastUpdatedLocation);
                }
                break;
            case (1):
                handleException(e);
                break;
            case (2):
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
    protected void resetElementsState() {
        editDelay.setEnabled(true);
        stopUpdatesButton.setEnabled(false);
        requestUpdatesButton.setEnabled(true);
        waitingMessage.setVisibility(View.INVISIBLE);
        tvTimer.setVisibility(View.INVISIBLE);
        displayMapButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        stopService(intent);
        super.onDestroy();
    }

    private void stopTimer() {
        if (cTimer != null)
            cTimer.cancel();
        LocationProvider locationProvider = UpdateService.getLocationProvider();
        if (locationProvider != null)
            locationProvider.stopLocationUpdates();
    }

    private void startTimer(double minutesInterval) {
        waitingMessage.setVisibility(View.VISIBLE);
        tvTimer.setVisibility(View.VISIBLE);
        long millis = (long) (minutesInterval * 60 * 1000);
        cTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutesLeft);
                tvTimer.setText(getResources().getString(R.string.timer_start_value, minutesLeft, secondsLeft));
            }

            public void onFinish() {
            }
        };
        cTimer.start();
    }
}
