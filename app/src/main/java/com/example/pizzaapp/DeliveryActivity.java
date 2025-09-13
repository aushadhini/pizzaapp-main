package com.example.pizzaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DeliveryActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_PLACE_ID = "extra_place_id";


    private GoogleMap map;
    private FusedLocationProviderClient fused;
    private LatLng selected;
    private Marker selectedMarker;
    private TextView tvAddress;
    private Button btnConfirm;

    private final ActivityResultLauncher<String> locationPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) enableMyLocationAndCenter();
                else
                    Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        tvAddress = findViewById(R.id.tvAddress);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> returnSelection());

        fused = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (fragment != null) fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMapLongClickListener(latLng -> {
            dropOrMovePin(latLng);
            tvAddress.setText(resolveAddress(latLng));
        });

        ensureLocationPermission();
    }

    private void ensureLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPerm.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            enableMyLocationAndCenter();
        }
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocationAndCenter() {
        if (map == null) return;
        map.setMyLocationEnabled(true);

        fused.getLastLocation().addOnSuccessListener(this, loc -> {
            LatLng target = new LatLng(6.9271, 79.8612); // Colombo fallback
            float zoom = 13f;

            if (loc != null) {
                target = new LatLng(loc.getLatitude(), loc.getLongitude());
                zoom = 16f;
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, zoom));
        });
    }

    private void dropOrMovePin(LatLng latLng) {
        selected = latLng;
        if (selectedMarker == null) {
            selectedMarker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Delivery location")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            selectedMarker.setPosition(latLng);
        }
    }

    private String resolveAddress(LatLng latLng) {
        try {
            Geocoder gc = new Geocoder(this, Locale.getDefault());
            List<Address> results = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (results != null && !results.isEmpty()) {
                Address a = results.get(0);
                String line = a.getAddressLine(0);
                return line != null ? line : a.getLocality();
            }
        } catch (IOException ignored) {
        }
        return "Pinned: " + latLng.latitude + ", " + latLng.longitude;
    }

    private void returnSelection() {
        if (selected == null) {
            Toast.makeText(this, "Long-press on the map to choose a location", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_LAT, selected.latitude);
        data.putExtra(EXTRA_LNG, selected.longitude);
        data.putExtra(EXTRA_ADDRESS, tvAddress.getText().toString());
        setResult(RESULT_OK, data);
        finish();
    }
    // Call this when the user confirms the picked place:
}
