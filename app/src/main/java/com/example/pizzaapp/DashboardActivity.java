package com.example.pizzaapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import android.location.LocationManager;
import android.provider.Settings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "Dashboard";

    // ---------- Popular ----------
    private RecyclerView rvPopular;
    private TextView emptyView;
    private final List<Food> itemsPopular = new ArrayList<>();
    private FoodAdapter adapterPopular;
    private ListenerRegistration regPopular;

    // ---------- Nearest ----------
    private RecyclerView rvNearest;
    private TextView txtNearestBranch;
    private final List<Food> itemsNearest = new ArrayList<>();
    private FoodAdapter adapterNearest;
    private ListenerRegistration regNearest;

    // ---------- Firebase / Location ----------
    private FirebaseFirestore db;
    private FusedLocationProviderClient fused;
    private Branch chosenNearestBranch;

    // Ask for location permissions at runtime
    private final ActivityResultLauncher<String[]> locationPermsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (fine || coarse) {
                    getLocationAndBindNearest();
                } else {
                    if (txtNearestBranch != null) {
                        txtNearestBranch.setText("Location permission denied");
                    }
                    // Optional fallback: listenNearestMenuFor("Colombo Branch");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = FirebaseFirestore.getInstance();
        fused = LocationServices.getFusedLocationProviderClient(this);

        // ---------- Bind views ----------
        rvPopular = findViewById(R.id.rvPopular);
        emptyView = findViewById(R.id.emptyView);

        rvNearest = findViewById(R.id.rvNearest);
        txtNearestBranch = findViewById(R.id.txtNearestBranch); // add in XML under "Nearest"

        // ---------- Popular adapter ----------
        rvPopular.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        adapterPopular = new FoodAdapter(itemsPopular, new FoodAdapter.FoodListener() {
            @Override public void onAdd(Food f) {
                Toast.makeText(DashboardActivity.this,
                        "Added: " + (f.name == null ? "" : f.name),
                        Toast.LENGTH_SHORT).show();
            }
            @Override public void onFavToggle(Food f, boolean nowFav) { /* optional persist */ }
        });
        rvPopular.setAdapter(adapterPopular);

        // ---------- Nearest adapter ----------
        rvNearest.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        adapterNearest = new FoodAdapter(itemsNearest, new FoodAdapter.FoodListener() {
            @Override public void onAdd(Food f) {
                Toast.makeText(DashboardActivity.this,
                        "Added: " + (f.name == null ? "" : f.name),
                        Toast.LENGTH_SHORT).show();
            }
            @Override public void onFavToggle(Food f, boolean nowFav) { /* optional persist */ }
        });
        rvNearest.setAdapter(adapterNearest);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Popular: listen to a known branch (or global "popular" source if you have one)
        listenPopularFromBranch("Colombo Branch");

        // Nearest: ensure permission -> get location -> pick nearest -> listen its menu
        ensureLocationAndBindNearest();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (regPopular != null) { regPopular.remove(); regPopular = null; }
        if (regNearest != null) { regNearest.remove(); regNearest = null; }
    }

    // ========================= Popular =========================
    private void listenPopularFromBranch(String branchId) {
        DocumentReference branchDoc = db.collection("branches").document(branchId);
        regPopular = branchDoc.collection("menu")
                .addSnapshotListener((@Nullable QuerySnapshot snaps,
                                      @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) -> {
                    itemsPopular.clear();
                    if (e != null) {
                        Log.e(TAG, "Popular menu error", e);
                    } else if (snaps != null) {
                        for (QueryDocumentSnapshot d : snaps) {
                            Food f = d.toObject(Food.class);
                            if (f != null) {
                                if (f.subtitle == null) f.subtitle = "";
                                itemsPopular.add(f);
                            }
                        }
                    }
                    adapterPopular.notifyDataSetChanged();
                    updatePopularEmptyState();
                });
    }

    private void updatePopularEmptyState() {
        if (itemsPopular.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            rvPopular.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            rvPopular.setVisibility(View.VISIBLE);
        }
    }

    // ========================= Nearest =========================
    private void ensureLocationAndBindNearest() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            getLocationAndBindNearest();
        } else {
            locationPermsLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getLocationAndBindNearest() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!(fine || coarse)) {
            if (txtNearestBranch != null) txtNearestBranch.setText("Location permission not granted");
            return;
        }

        // If Play Services missing/broken → fallback
        if (!isPlayServicesOk()) {
            Log.w(TAG, "Google Play services unavailable. Using LocationManager fallback.");
            getLocationWithLocationManagerFallback();   // <- defined below
            return;
        }

        try {
            fused.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) {
                    fetchBranchesAndPickNearest(loc);
                } else {
                    if (txtNearestBranch != null) txtNearestBranch.setText("Getting GPS…");
                    CancellationTokenSource cts = new CancellationTokenSource();
                    fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                            .addOnSuccessListener(current -> {
                                if (current != null) {
                                    fetchBranchesAndPickNearest(current);
                                } else {
                                    if (txtNearestBranch != null) txtNearestBranch.setText("Couldn’t get location");
                                }
                            })
                            .addOnFailureListener(err -> {
                                Log.e(TAG, "getCurrentLocation failed", err);
                                if (txtNearestBranch != null) txtNearestBranch.setText("Location error");
                            });
                }
            }).addOnFailureListener(err -> {
                Log.e(TAG, "getLastLocation failed", err);
                if (txtNearestBranch != null) txtNearestBranch.setText("Location error");
            });
        } catch (SecurityException se) {
            Log.e(TAG, "Location call without permission", se);
            if (txtNearestBranch != null) txtNearestBranch.setText("Location access denied");
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLocationWithLocationManagerFallback() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Try a quick last-known fix first
            Location last = null;
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (last == null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (last != null) {
                fetchBranchesAndPickNearest(last);
                return;
            }

            // API 30+ has requestLocationUpdates with one-shot getCurrentLocation on LM too,
            // but to keep it simple, prompt user to enable location if providers are off:
            if (!(lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                if (txtNearestBranch != null) txtNearestBranch.setText("Enable Location in Settings");
                // Optionally deep-link:
                // startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }

            // Minimal live request (NETWORK preferred for speed)
            final android.location.LocationListener listener = new android.location.LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    fetchBranchesAndPickNearest(location);
                    lm.removeUpdates(this);
                }
                @Override public void onProviderEnabled(String provider) { }
                @Override public void onProviderDisabled(String provider) { }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
            };

            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
            } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            } else {
                if (txtNearestBranch != null) txtNearestBranch.setText("No location provider available");
            }
        } catch (SecurityException se) {
            Log.e(TAG, "LocationManager fallback without permission", se);
            if (txtNearestBranch != null) txtNearestBranch.setText("Location access denied");
        }
    }



    private void fetchBranchesAndPickNearest(Location userLoc) {
        db.collection("branches").get().addOnSuccessListener(snaps -> {
            if (snaps == null || snaps.isEmpty()) {
                if (txtNearestBranch != null) txtNearestBranch.setText("No branches");
                return;
            }

            Branch nearest = null;
            float bestMeters = Float.MAX_VALUE;

            for (DocumentSnapshot d : snaps.getDocuments()) {
                Branch b = d.toObject(Branch.class);
                if (b == null) b = new Branch();
                b.id = d.getId();
                if (b.name == null || b.name.trim().isEmpty()) b.name = b.id;

                if (b.lat == null || b.lng == null) continue;

                float[] results = new float[1];
                Location.distanceBetween(
                        userLoc.getLatitude(), userLoc.getLongitude(),
                        b.lat, b.lng, results
                );
                float meters = results[0];
                if (meters < bestMeters) {
                    bestMeters = meters;
                    nearest = b;
                }
            }

            if (nearest == null) {
                if (txtNearestBranch != null) txtNearestBranch.setText("No geocoded branches");
                return;
            }

            chosenNearestBranch = nearest;
            String km = String.format(Locale.getDefault(), "%.1f km", bestMeters / 1000f);
            if (txtNearestBranch != null) txtNearestBranch.setText(nearest.name + " • " + km);

            listenNearestMenuFor(nearest.id);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Load branches failed", e);
            if (txtNearestBranch != null) txtNearestBranch.setText("Failed to load branches");
        });
    }

    /** Checks if Google Play services are available on the device */
    private boolean isPlayServicesOk() {
        int code = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);
        return code == ConnectionResult.SUCCESS;
    }


    private void listenNearestMenuFor(String branchId) {
        if (regNearest != null) { regNearest.remove(); regNearest = null; }

        DocumentReference branchDoc = db.collection("branches").document(branchId);
        regNearest = branchDoc.collection("menu")
                .addSnapshotListener((snaps, e) -> {
                    itemsNearest.clear();
                    if (e != null) {
                        Log.e(TAG, "Nearest menu error", e);
                        Toast.makeText(this, "Nearest menu failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } else if (snaps != null) {
                        for (QueryDocumentSnapshot d : snaps) {
                            Food f = d.toObject(Food.class);
                            if (f != null) {
                                if (f.subtitle == null) f.subtitle = "";
                                itemsNearest.add(f);
                            }
                        }
                    }
                    adapterNearest.notifyDataSetChanged();
                });
    }
}
