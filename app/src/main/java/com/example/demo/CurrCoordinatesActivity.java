package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geopositionmodule.ILocationCallback;
import com.example.geopositionmodule.LatLng;
import com.example.geopositionmodule.LocationProvider;
import com.example.geopositionmodule.exceptions.LocationProviderDisabledException;
import com.example.geopositionmodule.exceptions.NoLocationAccessException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class CurrCoordinatesActivity extends Activity implements Alertable, ActivityCompat.OnRequestPermissionsResultCallback {
    private Button showToastButton;
    private LocationProvider locationProvider;
    private ProgressBar progressBar;
    private TextView progressMessage;
    public static final int REQUEST_LOCATION_PERMISSION_SUCCESS = 0;
    static boolean IS_PERMISSION_REQUESTED_FIRST_TIME = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_coordinates);
        showToastButton = findViewById(R.id.request_curr_coordinates_button);
        progressBar = findViewById(R.id.progressBar);
        progressMessage = findViewById(R.id.request_in_progress_message);
       // try {
            locationProvider = new LocationProvider(CurrCoordinatesActivity.this);
//        } catch (GooglePlayServicesNotAvailableException e) {
//            displayAlert(e.getMessage(), CurrCoordinatesActivity.this, true);
//            e.printStackTrace();
//        }

        Button.OnClickListener listener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToastButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                progressMessage.setVisibility(View.VISIBLE);
                try {
                    ILocationCallback myCallback = new ILocationCallback() {
                        @Override
                        public void callOnSuccess(LatLng coordinates) {
                            Toast toast = Toast.makeText(getApplicationContext(), coordinates.toString(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 400);
                            toast.show();
                            resetElementsState();
                        }

                        @Override
                        public void callOnFail(Exception e) {
                            e.printStackTrace();
                            resetElementsState();
                            displayAlert(e.getMessage(), CurrCoordinatesActivity.this, false);
                        }
                    };
                    locationProvider.requestCurrentLocation(myCallback);
                } catch (NoLocationAccessException e) {
                    if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME) {
                            CurrCoordinatesActivity.IS_PERMISSION_REQUESTED_FIRST_TIME = false;
                        }
                        ActivityCompat.requestPermissions(CurrCoordinatesActivity.this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, REQUEST_LOCATION_PERMISSION_SUCCESS);
                    } else {
                        e.printStackTrace();
                        resetElementsState();
                        displayAlert(e.getMessage(), CurrCoordinatesActivity.this, false);
                    }

                } catch (LocationProviderDisabledException | NullPointerException e) {
                    e.printStackTrace();
                    resetElementsState();
                    displayAlert(e.getMessage(), CurrCoordinatesActivity.this, false);
                }
            }
        };
        showToastButton.setOnClickListener(listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        resetElementsState();
        if (requestCode == REQUEST_LOCATION_PERMISSION_SUCCESS) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //Permission Denied!
                displayAlert(NoLocationAccessException.message, CurrCoordinatesActivity.this, false);
            }
        }
    }

    private void resetElementsState() {
        progressBar.setVisibility(View.INVISIBLE);
        progressMessage.setVisibility(View.INVISIBLE);
        showToastButton.setEnabled(true);
    }
}
