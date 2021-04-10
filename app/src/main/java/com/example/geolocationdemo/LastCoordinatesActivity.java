package com.example.geolocationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.LocationReceiver;

public class LastCoordinatesActivity extends Activity implements Alertable {
    private Button showToastButton;
    private LocationReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_coordinates);
        showToastButton = findViewById(R.id.request_last_coordinates_button);
        locationReceiver = new LocationReceiver(LastCoordinatesActivity.this);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast toast = Toast.makeText(getApplicationContext(), locationReceiver.getLastKnownLocation().toString(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 400);
                    toast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    displayAlert("Последние координаты не были найдены (location = null).", LastCoordinatesActivity.this);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
    }
}
