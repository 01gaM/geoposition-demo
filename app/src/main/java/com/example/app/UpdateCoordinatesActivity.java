package com.example.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.AccuracyPriority;
import com.example.geopositionmodule.LatLng;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class UpdateCoordinatesActivity extends ServiceBinder {
    private Button requestUpdatesButton;
    private Button stopUpdatesButton;
    private EditText editDelay;
    private TextView tvTimer;
    private CountDownTimer cTimer = null;
    private TextView waitingMessage;
    private Button displayMapButton;
    private LatLng currCoordinates = null;
    private MapDialog mapDialog;
    private Menu menu;
    private ProgressBar progressBar;
    private boolean shouldShowProgressBar = true; //progress bar should be shown only before first location fix
    private final int UPDATE_REQUEST_CODE = 1;

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
        progressBar = findViewById(R.id.progress_bar_update);
        setRequestCode(UPDATE_REQUEST_CODE);
        doBindService();

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
                waitingMessage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                editDelay.setEnabled(false);
                requestUpdatesButton.setEnabled(false);
                stopUpdatesButton.setEnabled(true);
                menu.setGroupEnabled(R.id.menu_group, false);
                if (cTimer != null)
                    cTimer.cancel();
                double intervalMin = Double.parseDouble(editDelay.getText().toString());
                intent.putExtra("intervalMin", intervalMin);
                startService(intent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accuracy_priority_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        AccuracyPriority accuracyPriority;
        switch (item.getItemId()) {
            case R.id.menu_priority_balanced_power_accuracy:
                accuracyPriority = AccuracyPriority.PRIORITY_BALANCED_POWER_ACCURACY;
                break;
            case R.id.menu_priority_low_power:
                accuracyPriority = AccuracyPriority.PRIORITY_LOW_POWER;
                break;
            default:
                accuracyPriority = AccuracyPriority.PRIORITY_HIGH_ACCURACY;
        }
        if (locationProvider != null) {
            locationProvider.setAccuracyPriority(accuracyPriority);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_REQUEST_CODE) {
            Exception e = updateService.getCurrException();
            switch (resultCode) {
                case (UpdateService.UPDATE_SUCCEEDED):
                    if (shouldShowProgressBar) {
                        double minutes = Double.parseDouble(editDelay.getText().toString());
                        startTimer(minutes);
                        progressBar.setVisibility(View.INVISIBLE);
                        shouldShowProgressBar = false;
                    }
                    LatLng lastUpdatedLocation = updateService.getLocation();
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
    protected void resetElementsState() {
        editDelay.setEnabled(true);
        stopUpdatesButton.setEnabled(false);
        requestUpdatesButton.setEnabled(true);
        waitingMessage.setVisibility(View.INVISIBLE);
        tvTimer.setVisibility(View.INVISIBLE);
        displayMapButton.setEnabled(false);
        menu.setGroupEnabled(R.id.menu_group, true);
        progressBar.setVisibility(View.INVISIBLE);
        shouldShowProgressBar = true;
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        if (intent != null) {
            stopService(intent);
        }
        doUnbindService();
        super.onDestroy();
    }

    private void stopTimer() {
        if (cTimer != null)
            cTimer.cancel();
        if (locationProvider != null)
            locationProvider.stopLocationUpdates();
        if (updateService != null) {
            updateService.stopForeground(true);
        }
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
