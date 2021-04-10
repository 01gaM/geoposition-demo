package com.example.geolocationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.LocationReceiver;

public class CurrCoordinatesActivity extends Activity implements Alertable {
    private Button showToastButton;
    private LocationReceiver locationReceiver;
    private ProgressBar progressBar;
    private TextView progressMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_coordinates);
        showToastButton = findViewById(R.id.request_curr_coordinates_button);
        progressBar = findViewById(R.id.progressBar);
        progressMessage = findViewById(R.id.request_in_progress_message);
        locationReceiver = new LocationReceiver(CurrCoordinatesActivity.this);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                progressMessage.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() { //10 sec delay for progress bar demo
                    public void run() {
                        try {
                            Toast toast = Toast.makeText(getApplicationContext(), locationReceiver.requestCurrentLocation().toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show(); // Actions to do after 10 seconds
                        } catch (Exception e) {
                            e.printStackTrace();
                            displayAlert("Текущие координаты не были найдены (currLocation = null).", CurrCoordinatesActivity.this);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        progressMessage.setVisibility(View.INVISIBLE);
                    }
                }, 10000);
            }
        };
        showToastButton.setOnClickListener(listener);
    }
}
