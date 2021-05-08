package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.GooglePlayServicesNotAvailableException;


public class MainActivity extends AppCompatActivity implements Alertable {
    private Button currCoordinatesButton;
    private Button lastCoordinatesButton;
    private Button updateCoordinatesButton;
    private Button getSpeedButton;

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

        checkGooglePlayServices();

        currCoordinatesButton = findViewById(R.id.button_curr_coordinates);
        Button.OnClickListener currListener = generateButtonOnClickListener(CurrCoordinatesActivity.class);
        currCoordinatesButton.setOnClickListener(currListener);

        lastCoordinatesButton = findViewById(R.id.button_last_coordinates);
        Button.OnClickListener lastListener = generateButtonOnClickListener(LastCoordinatesActivity.class);
        lastCoordinatesButton.setOnClickListener(lastListener);

        updateCoordinatesButton = findViewById(R.id.button_update_coordinates);
        Button.OnClickListener updateListener = generateButtonOnClickListener(UpdateCoordinatesActivity.class);
        updateCoordinatesButton.setOnClickListener(updateListener);

        getSpeedButton = findViewById(R.id.button_get_speed);
        Button.OnClickListener speedListener = generateButtonOnClickListener(SpeedActivity.class);
        getSpeedButton.setOnClickListener(speedListener);
    }

    private void checkGooglePlayServices() {
        boolean firstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
        if (firstRun) {
            try {
                LocationProvider.checkGooglePlayServicesAvailable(this);
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
                displayAlert("Приложение работает медленнее без использования сервисов Google Play! " + e.getMessage(),
                        "Предупреждение", this);
            }
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstrun", false)
                    .apply();
        }
    }
}