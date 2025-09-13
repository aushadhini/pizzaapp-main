package com.example.pizzaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;

public class PickupActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String EXTRA_BRANCH_NAME = "extra_branch_name";
    public static final String EXTRA_BRANCH_LAT = "extra_branch_lat";
    public static final String EXTRA_BRANCH_LNG = "extra_branch_lng";

    private GoogleMap map;
    private FusedLocationProviderClient fused;

    private TextView tvSelected;
    private Button btnSelect, btnDirections;

    private final List<Branch> branches = new ArrayList<>();
    private Branch selectedBranch;

    private final ActivityResultLauncher<String> locationPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) enableMyLocationAndCenter();
                else Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);

        tvSelected = findViewById(R.id.tvSelectedOutlet);
        btnSelect = findViewById(R.id.btnSelectOutlet);
        btnDirections = findViewById(R.id.btnDirections);

        btnSelect.setOnClickListener(v -> returnSelection());
        btnDirections.setOnClickListener(v -> openDirections());

        fused = LocationServices.getFusedLocationProviderClient(this);
        seedBranches();

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (fragment != null) fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.getUiSettings().setZoomControlsEnabled(true);

        addBranchMarkers();
        ensureLocationPermission();
    }

    private void addBranchMarkers() {
        if (map == null) return;
        for (Branch b : branches) {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(b.lat, b.lng))
                    .title(b.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            if (m != null) m.setTag(b);
        }
        // Focus on Sri Lanka center-ish
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.8731, 80.7718), 6.8f));
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
        fused.getLastLocation().addOnSuccessListener(this, this::highlightNearest);
    }

    private void highlightNearest(Location userLoc) {
        if (userLoc == null || branches.isEmpty()) return;
        Branch best = branches.get(0);
        double bestD = distanceKm(userLoc.getLatitude(), userLoc.getLongitude(), best.lat, best.lng);
        for (int i = 1; i < branches.size(); i++) {
            Branch b = branches.get(i);
            double d = distanceKm(userLoc.getLatitude(), userLoc.getLongitude(), b.lat, b.lng);
            if (d < bestD) { best = b; bestD = d; }
        }
        selectBranch(best);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(best.lat, best.lng), 15f));
        Toast.makeText(this, "Nearest: " + best.name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Branch) {
            selectBranch((Branch) tag);
            return true; // we handled it
        }
        return false;
    }

    private void selectBranch(Branch b) {
        selectedBranch = b;
        tvSelected.setText(b.name + " • " + b.lat + "," + b.lng);
    }

    private void returnSelection() {
        if (selectedBranch == null) {
            Toast.makeText(this, "Tap a branch marker to select", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_BRANCH_NAME, selectedBranch.name);
        data.putExtra(EXTRA_BRANCH_LAT, selectedBranch.lat);
        data.putExtra(EXTRA_BRANCH_LNG, selectedBranch.lng);
        setResult(RESULT_OK, data);
        finish();
    }

    private void openDirections() {
        if (selectedBranch == null) {
            Toast.makeText(this, "Select an outlet first", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.parse("google.navigation:q=" + selectedBranch.lat + "," + selectedBranch.lng);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        startActivity(i);
    }

    // --- utils / data ---
    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private void seedBranches() {
        branches.add(new Branch("Pizza Outlet - Galle", 6.0535, 80.2210));
        branches.add(new Branch("Pizza Outlet - Nugegoda", 6.8610, 79.8917));
        branches.add(new Branch("Pizza Outlet - Kandy", 7.2906, 80.6337));
        // add your real outlets…
    }

    private static class Branch {
        final String name; final double lat, lng;
        Branch(String name, double lat, double lng) { this.name = name; this.lat = lat; this.lng = lng; }
    }
}
