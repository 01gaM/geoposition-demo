package com.example.geolocationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.LocationReceiver;

public class LastCoordinatesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_coordinates);
        final Button showToastButton = findViewById(R.id.request_last_coordinates_button);
        final LocationReceiver locationReceiver = new LocationReceiver(LastCoordinatesActivity.this);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getApplicationContext(), locationReceiver.getLastKnownLocation().toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 400);
                toast.show();
            }
        };
        showToastButton.setOnClickListener(listener);
    }
}
