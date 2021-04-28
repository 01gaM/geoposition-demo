package com.example.demo;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.example.geopositionmodule.LatLng;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MapDialog extends Dialog {
    private Button closeButton;
    private MapView mapView;
    private LatLng startPoint;

    public MapDialog(@NonNull Context context, LatLng coordinates) {
        super(context);
        this.startPoint = coordinates;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_map);
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        setCurrentPoint(startPoint);
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.5);
        closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void setCurrentPoint(LatLng newPoint) {
        GeoPoint newGeoPoint = new GeoPoint(newPoint.getLatitude(), newPoint.getLongitude());
        IMapController mapController = mapView.getController();
        mapController.setCenter(newGeoPoint);
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(newGeoPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (!mapView.getOverlays().isEmpty()) {
            mapView.getOverlays().remove(0);
        }
        mapView.getOverlays().add(startMarker);
    }
}
