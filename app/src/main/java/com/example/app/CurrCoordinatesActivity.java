package com.example.app;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.AccuracyPriority;
import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationSupplier;
import com.example.geopositionmodule.exceptions.AirplaneModeOnException;
import com.example.geopositionmodule.exceptions.DeviceLocationDisabledException;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.LocationPermissionNotGrantedException;

import androidx.annotation.NonNull;

public class CurrCoordinatesActivity extends BaseCoordinatesActivity {
    private Button sendRequestButton;
    private Button cancelRequestButton;
    private ProgressBar progressBar;
    private TextView progressMessage;
    private Button displayMapButton;
    private LatLng currCoordinates = null;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_coordinates);
        sendRequestButton = findViewById(R.id.request_curr_coordinates_button);
        cancelRequestButton = findViewById(R.id.button_cancel_request);
        progressBar = findViewById(R.id.progressBar);
        progressMessage = findViewById(R.id.request_in_progress_message);
        displayMapButton = findViewById(R.id.button_display_map);
        locationSupplier = new LocationSupplier(CurrCoordinatesActivity.this);

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequestButton.setEnabled(false);
                cancelRequestButton.setEnabled(true);
                progressBar.setVisibility(View.VISIBLE);
                progressMessage.setVisibility(View.VISIBLE);
                menu.setGroupEnabled(R.id.menu_group, false);

                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng coordinates) {
                            currCoordinates = coordinates;
                            Toast toast = Toast.makeText(getApplicationContext(), coordinates.toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show();
                            resetElementsState();
                            displayMapButton.setEnabled(true);
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            handleException(e);
                        }
                    };
                    locationSupplier.requestCurrentLocation(myCallback);
                } catch (LocationPermissionNotGrantedException e) {
                    if (isPermissionRequestedFirstTime || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions();
                    } else {
                        handleException(e);
                    }
                } catch (LocationProviderDisabledException | AirplaneModeOnException | DeviceLocationDisabledException e) {
                    handleException(e);
                }
            }
        };
        sendRequestButton.setOnClickListener(listener);
        displayMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new MapDialog(CurrCoordinatesActivity.this, currCoordinates);
                dialog.setCancelable(true);
                dialog.show();
            }
        });
        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationSupplier.cancelCurrentLocationRequest();
                resetElementsState();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accuracy_priority_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.menu_priority_balanced_power_accuracy:
                locationSupplier.setAccuracyPriority(AccuracyPriority.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case R.id.menu_priority_low_power:
                locationSupplier.setAccuracyPriority(AccuracyPriority.PRIORITY_LOW_POWER);
                break;
            default:
                locationSupplier.setAccuracyPriority(AccuracyPriority.PRIORITY_HIGH_ACCURACY);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void resetElementsState() {
        progressBar.setVisibility(View.INVISIBLE);
        progressMessage.setVisibility(View.INVISIBLE);
        sendRequestButton.setEnabled(true);
        cancelRequestButton.setEnabled(false);
        menu.setGroupEnabled(R.id.menu_group, true);
    }

    @Override
    protected void onDestroy() {
        if (cancelRequestButton.isEnabled()){
            locationSupplier.cancelCurrentLocationRequest();
        }
        super.onDestroy();
    }
}
