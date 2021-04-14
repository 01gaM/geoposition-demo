package com.example.geolocationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.NoLocationAccessException;

public class CurrCoordinatesActivity extends Activity implements Alertable {
    private Button showToastButton;
    private LocationProvider locationReceiver;
    private ProgressBar progressBar;
    private TextView progressMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_coordinates);
        showToastButton = findViewById(R.id.request_curr_coordinates_button);
        progressBar = findViewById(R.id.progressBar);
        progressMessage = findViewById(R.id.request_in_progress_message);
        try {
            locationReceiver = new LocationProvider(CurrCoordinatesActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                progressMessage.setVisibility(View.VISIBLE);
                try {
                    LatLng currCoordinates = locationReceiver.requestCurrentLocation();
                    Toast toast = Toast.makeText(getApplicationContext(), currCoordinates.toString(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 400);
                    toast.show();
                } catch (NullPointerException | NoLocationAccessException e) {
                    e.printStackTrace();
                    displayAlert(e.getMessage(), CurrCoordinatesActivity.this);
                }
                progressBar.setVisibility(View.INVISIBLE);
                progressMessage.setVisibility(View.INVISIBLE);
            }
        };
        showToastButton.setOnClickListener(listener);
    }
}
