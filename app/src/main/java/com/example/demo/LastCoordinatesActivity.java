package com.example.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.AccuracyPriority;
import com.example.geopositionmodule.GooglePlayServicesNotAvailableException;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.NoLocationAccessException;

public class LastCoordinatesActivity extends Activity implements Alertable {
    private Button showToastButton;
    private LocationProvider locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_coordinates);
        showToastButton = findViewById(R.id.request_last_coordinates_button);
        try {
            locationReceiver = new LocationProvider(LastCoordinatesActivity.this);
        } catch (GooglePlayServicesNotAvailableException e) {
            displayAlert(e.getMessage(), LastCoordinatesActivity.this, true);
            e.printStackTrace();
        }

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LatLng lastCoordinates = locationReceiver.getLastKnownLocation();
                    Toast toast = Toast.makeText(getApplicationContext(), lastCoordinates.toString(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 400);
                    toast.show();
                } catch (NoLocationAccessException e) {
                    e.printStackTrace();
                    displayAlert(e.getMessage(), LastCoordinatesActivity.this, true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    displayAlert(e.getMessage(), LastCoordinatesActivity.this, false);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
    }
}