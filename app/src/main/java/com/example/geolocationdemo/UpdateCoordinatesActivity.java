package com.example.geolocationdemo;

import android.app.Activity;
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

import com.example.geopositionmodule.LocationReceiver;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UpdateCoordinatesActivity extends Activity implements Alertable {
    private Button showToastButton;
    private EditText editDelay;
    private TextView tvTimer;
    private CountDownTimer cTimer = null;
    private TextView waitingMessage;
    private final double MIN_UPDATE_INTERVAL = 0.1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_coordinates);
        showToastButton = findViewById(R.id.request_coordinates_updates_button);
        tvTimer = findViewById(R.id.timerTextView);
        waitingMessage = findViewById(R.id.waitingMessageTextView);
        editDelay = findViewById(R.id.editTextNumber);
        editDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!editDelay.getText().toString().isEmpty()) {
                    showToastButton.setEnabled(true);
                } else {
                    showToastButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // final LocationReceiver locationReceiver = new LocationReceiver(UpdateCoordinatesActivity.this);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cTimer != null)
                    cTimer.cancel();
                double minutes = Double.parseDouble(editDelay.getText().toString());
                if (minutes < MIN_UPDATE_INTERVAL) {
                    displayAlert(String.format(Locale.US, "Минимальное допустимое значение интервала = %.1f мин", MIN_UPDATE_INTERVAL), UpdateCoordinatesActivity.this);
                    waitingMessage.setVisibility(View.INVISIBLE);
                    tvTimer.setVisibility(View.INVISIBLE);
                } else {
                    startTimer(minutes);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        if (cTimer != null)
            cTimer.cancel();
        super.onDestroy();
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
                showToastButton.setEnabled(true);
                Toast toast = Toast.makeText(UpdateCoordinatesActivity.this, String.valueOf(minutesInterval), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 400);
                toast.show();
                cTimer.start(); //restart timer
            }
        };
        cTimer.start();
    }
}
