package com.example.geolocationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.LocationReceiver;

public class UpdateCoordinatesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_coordinates);
        final Button showToastButton = findViewById(R.id.request_coordinates_updates_button);
       // final LocationReceiver locationReceiver = new LocationReceiver(UpdateCoordinatesActivity.this);
        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //locationReceiver.requestLocationUpdates(10).
                Toast toast = Toast.makeText(getApplicationContext(), "Click!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 400);
                toast.show();
            }
        };
        showToastButton.setOnClickListener(listener);
    }
}
