package com.example.geolocationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.geopositionmodule.Test;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button showToastButton = findViewById(R.id.button);

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(Test.sum(5, 10)), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 400);
                toast.show();
            }
        };
        showToastButton.setOnClickListener(listener);
    }


}