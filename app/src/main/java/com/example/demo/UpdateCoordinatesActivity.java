package com.example.demo;

import android.Manifest;
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

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.exceptions.IntervalValueOutOfRangeException;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

import java.util.concurrent.TimeUnit;

public class UpdateCoordinatesActivity extends BaseCoordinatesActivity {
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
        locationProvider = new LocationProvider(UpdateCoordinatesActivity.this);
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
                            handleException(e);
                        }
                    };
                    locationProvider.requestLocationUpdates(minutes, myCallback);
                    startTimer(minutes);
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
        requestUpdatesButton.setOnClickListener(listener);
        stopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                resetElementsState();
//                waitingMessage.setVisibility(View.INVISIBLE);
//                tvTimer.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void resetElementsState() {
        editDelay.setEnabled(true);
        stopUpdatesButton.setEnabled(false);
        requestUpdatesButton.setEnabled(true);
        waitingMessage.setVisibility(View.INVISIBLE);
        tvTimer.setVisibility(View.INVISIBLE);
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
