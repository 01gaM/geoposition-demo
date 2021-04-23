package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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

import com.example.geopositionmodule.GooglePlayServicesNotAvailableException;
import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.LocationProviderDisabledException;
import com.example.geopositionmodule.NoLocationAccessException;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class UpdateCoordinatesActivity extends Activity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback {
    private Button requestUpdatesButton;
    private Button stopUpdatesButton;
    private EditText editDelay;
    private TextView tvTimer;
    private CountDownTimer cTimer = null;
    private TextView waitingMessage;
    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_coordinates);
        requestUpdatesButton = findViewById(R.id.button_request_updates);
        stopUpdatesButton = findViewById(R.id.button_stop_updates);
        tvTimer = findViewById(R.id.text_timer);
        waitingMessage = findViewById(R.id.text_waiting_message);
        editDelay = findViewById(R.id.edit_text_interval);
        try {
            locationProvider = new LocationProvider(UpdateCoordinatesActivity.this);
        } catch (GooglePlayServicesNotAvailableException e) {
            displayAlert(e.getMessage(), UpdateCoordinatesActivity.this, true);
            e.printStackTrace();
        }
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
                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng lastUpdatedLocation) {
                            Toast toast = Toast.makeText(UpdateCoordinatesActivity.this, lastUpdatedLocation.toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show();
                            cTimer.start(); //restart timer
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            e.printStackTrace();
                            waitingMessage.setVisibility(View.INVISIBLE);
                            tvTimer.setVisibility(View.INVISIBLE);
                            displayAlert(e.getMessage(), UpdateCoordinatesActivity.this, false);
                            resetElementsState();
                        }
                    };
                    locationProvider.requestLocationUpdates(minutes, myCallback);
                    startTimer(minutes);
                } catch (NoLocationAccessException e) {
                    if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME) {
                            CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME = false;
                        }
                        ActivityCompat.requestPermissions(UpdateCoordinatesActivity.this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS);
                    } else {
                        e.printStackTrace();
                        displayAlert(e.getMessage(), UpdateCoordinatesActivity.this, false);
                        resetElementsState();
                    }
                } catch (LocationProviderDisabledException | IntervalValueOutOfRangeException e) {
                    e.printStackTrace();
                    displayAlert(e.getMessage(), UpdateCoordinatesActivity.this, false);
                    resetElementsState();
                }
            }
        };
        requestUpdatesButton.setOnClickListener(listener);
        stopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                resetElementsState();
                waitingMessage.setVisibility(View.INVISIBLE);
                tvTimer.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void resetElementsState(){
        editDelay.setEnabled(true);
        stopUpdatesButton.setEnabled(false);
        requestUpdatesButton.setEnabled(true);
    }


    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    private void stopTimer() {
        if (cTimer != null)
            cTimer.cancel();
        if (locationProvider != null)
            locationProvider.stopLocationUpdates();
    }

    void startTimer(double minutesInterval) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CurrCoordinatesActivity.REQUEST_LOCATION_PERMISSION_SUCCESS) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //Permission Denied!
                displayAlert(NoLocationAccessException.message, UpdateCoordinatesActivity.this, false);
            }
        }
    }
}
