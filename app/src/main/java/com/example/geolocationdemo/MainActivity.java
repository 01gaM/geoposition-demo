package com.example.geolocationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button currCoordinatesButton = findViewById(R.id.button_curr_coordinates);
        Button.OnClickListener currListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CurrCoordinatesActivity.class);
                startActivity(intent);
            }
        };
        currCoordinatesButton.setOnClickListener(currListener);

        final Button lastCoordinatesButton = findViewById(R.id.button_last_coordinates);
        Button.OnClickListener lastListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LastCoordinatesActivity.class);
                startActivity(intent);
            }
        };
        lastCoordinatesButton.setOnClickListener(lastListener);

        final Button updateCoordinatesButton = findViewById(R.id.button_update_coordinates);
        Button.OnClickListener updateListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UpdateCoordinatesActivity.class);
                startActivity(intent);
            }
        };
        updateCoordinatesButton.setOnClickListener(updateListener);
    }
}