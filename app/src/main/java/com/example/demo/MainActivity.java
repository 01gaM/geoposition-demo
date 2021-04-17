package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private Button currCoordinatesButton;
    private Button lastCoordinatesButton;
    private Button updateCoordinatesButton;

    private Button.OnClickListener generateButtonOnClickListener(Class<?> cls) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, cls);
                startActivity(intent);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currCoordinatesButton = findViewById(R.id.button_curr_coordinates);
        Button.OnClickListener currListener = generateButtonOnClickListener(CurrCoordinatesActivity.class);
        currCoordinatesButton.setOnClickListener(currListener);

        lastCoordinatesButton = findViewById(R.id.button_last_coordinates);
        Button.OnClickListener lastListener = generateButtonOnClickListener(LastCoordinatesActivity.class);
        lastCoordinatesButton.setOnClickListener(lastListener);

        updateCoordinatesButton = findViewById(R.id.button_update_coordinates);
        Button.OnClickListener updateListener = generateButtonOnClickListener(UpdateCoordinatesActivity.class);
        updateCoordinatesButton.setOnClickListener(updateListener);
    }
}