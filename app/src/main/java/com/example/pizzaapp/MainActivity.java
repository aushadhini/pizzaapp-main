package com.example.pizzaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
// If you want to gate profile by auth, uncomment next two lines
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Views
    private RecyclerView rvPopular, rvNearest;
    private TextView txtNearestBranch, txtSeeAllPopular, emptyView;
    private EditText edtSearch;
    private ImageButton btnFilter, navHome, navHeart, navCart, btnBell, navProfile;

    // Data
    private FoodAdapter popularAdapter, nearestAdapter;
    private final List<FoodItem> masterData = new ArrayList<>();
    private final List<FoodItem> popularData = new ArrayList<>();
    private final List<FoodItem> nearestData = new ArrayList<>();

    // Location
    private FusedLocationProviderClient fused;
    private final List<Branch> branches = new ArrayList<>();
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) fetchNearestBranch();
                else txtNearestBranch.setText("Allow location to detect nearest branch");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        setupRecyclerViews();
        seedDemoData();

        // default list
        popularData.clear();
        popularData.addAll(masterData);
        popularAdapter.notifyDataSetChanged();
        toggleEmpty();

        // Search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPopular(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Clicks
        btnFilter.setOnClickListener(v -> Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show());
        txtSeeAllPopular.setOnClickListener(v -> Toast.makeText(this, "See all popular", Toast.LENGTH_SHORT).show());
        btnBell.setOnClickListener(v -> Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());
        navHome.setOnClickListener(v -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());
        navHeart.setOnClickListener(v -> Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show());
        navCart.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AccountActivity.class)));

        // Location (optional)
        fused = LocationServices.getFusedLocationProviderClient(this);
        seedBranches();
        ensureLocationThenFetch();
    }

    private void bindViews() {
        txtNearestBranch = findViewById(R.id.txtNearestBranch);
        txtSeeAllPopular = findViewById(R.id.txtSeeAllPopular);
        emptyView = findViewById(R.id.emptyView);
        edtSearch = findViewById(R.id.edtSearch);
        btnFilter = findViewById(R.id.btnFilter);
        btnBell = findViewById(R.id.btnBell);

        rvPopular = findViewById(R.id.rvPopular);
        rvNearest = findViewById(R.id.rvNearest);

        navHome = findViewById(R.id.navHome);
        navHeart = findViewById(R.id.navHeart);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile); // <-- add this ID in your XML if missing
    }

    private void setupRecyclerViews() {
        rvPopular.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvNearest.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        popularAdapter = new FoodAdapter(popularData, new FoodAdapter.OnFoodClickListener() {
            @Override public void onItemClick(FoodItem item) {
                Intent intent = new Intent(MainActivity.this, PizzaDetailActivity.class);
                intent.putExtra(PizzaDetailActivity.EXTRA_TITLE, item.title);
                intent.putExtra(PizzaDetailActivity.EXTRA_SUBTITLE, item.subtitle);
                intent.putExtra(PizzaDetailActivity.EXTRA_RATING, item.rating);
                intent.putExtra(PizzaDetailActivity.EXTRA_PRICE, 1590.0);   // TODO real price
                intent.putExtra(PizzaDetailActivity.EXTRA_IMAGE_RES, item.imageRes);
                startActivity(intent);
            }
            @Override public void onLikeClick(FoodItem item) {
                item.liked = !item.liked;
                Toast.makeText(MainActivity.this,
                        (item.liked ? "Added to" : "Removed from") + " favorites",
                        Toast.LENGTH_SHORT).show();
            }
        });

        nearestAdapter = new FoodAdapter(nearestData, new FoodAdapter.OnFoodClickListener() {
            @Override public void onItemClick(FoodItem item) {
                Intent intent = new Intent(MainActivity.this, PizzaDetailActivity.class);
                intent.putExtra(PizzaDetailActivity.EXTRA_TITLE, item.title);
                intent.putExtra(PizzaDetailActivity.EXTRA_SUBTITLE, item.subtitle);
                intent.putExtra(PizzaDetailActivity.EXTRA_RATING, item.rating);
                intent.putExtra(PizzaDetailActivity.EXTRA_PRICE, 1590.0);   // TODO real price
                intent.putExtra(PizzaDetailActivity.EXTRA_IMAGE_RES, item.imageRes);
                startActivity(intent);
            }
            @Override public void onLikeClick(FoodItem item) { item.liked = !item.liked; }
        });

        rvPopular.setAdapter(popularAdapter);
        rvNearest.setAdapter(nearestAdapter);
    }

    private void seedDemoData() {
        // title, subtitle, rating, imageRes, reviews
        masterData.add(new FoodItem("Margherita", "Classic Pizza", 4.8f, R.drawable.pizza_10, 120));
        masterData.add(new FoodItem("Pepperoni", "Double Cheese", 4.7f, R.drawable.chickenpizza_1, 96));
        masterData.add(new FoodItem("Veggie", "Fresh Garden", 4.6f, R.drawable.pizza_9, 73));
        masterData.add(new FoodItem("BBQ Chicken", "Smoky & Sweet", 4.5f, R.drawable.pizza_13, 88));
        masterData.add(new FoodItem("Hawaiian", "Pineapple Hit", 4.2f, R.drawable.pizza_9, 51));
        masterData.add(new FoodItem("Meat Lovers", "Loaded Feast", 4.9f, R.drawable.pizza_10, 134));
    }

    private void filterPopular(String query) {
        popularData.clear();
        if (query == null || query.trim().isEmpty()) {
            popularData.addAll(masterData);
        } else {
            String q = query.toLowerCase();
            for (FoodItem it : masterData) {
                if (it.title.toLowerCase().contains(q) || it.subtitle.toLowerCase().contains(q)) {
                    popularData.add(it);
                }
            }
        }
        popularAdapter.notifyDataSetChanged();
        toggleEmpty();
    }

    private void toggleEmpty() {
        emptyView.setVisibility(popularData.isEmpty() ? View.VISIBLE : View.GONE);
        rvPopular.setVisibility(popularData.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ---------- Location ----------
    private void ensureLocationThenFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchNearestBranch();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchNearestBranch() {
        try {
            fused.getLastLocation().addOnSuccessListener(this, loc -> {
                if (loc == null) {
                    txtNearestBranch.setText("Can't get location. Showing popular nearby.");
                    fillNearestWithTopRated();
                    return;
                }
                Branch nearest = findNearestBranch(loc);
                txtNearestBranch.setText("Nearest: " + nearest.name);
                fillNearestWithTopRated();
            }).addOnFailureListener(e -> {
                txtNearestBranch.setText("Location error. Showing popular nearby.");
                fillNearestWithTopRated();
            });
        } catch (SecurityException se) {
            txtNearestBranch.setText("Location permission denied");
            fillNearestWithTopRated();
        }
    }

    private void seedBranches() {
        branches.add(new Branch("Galle", 6.0535, 80.2210));
        branches.add(new Branch("Nugegoda", 6.8610, 79.8917));
    }

    private Branch findNearestBranch(@NonNull Location user) {
        Branch best = branches.get(0);
        double bestDist = distanceKm(user.getLatitude(), user.getLongitude(), best.lat, best.lng);
        for (int i = 1; i < branches.size(); i++) {
            Branch b = branches.get(i);
            double d = distanceKm(user.getLatitude(), user.getLongitude(), b.lat, b.lng);
            if (d < bestDist) { best = b; bestDist = d; }
        }
        return best;
    }

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

    private void fillNearestWithTopRated() {
        nearestData.clear();
        List<FoodItem> copy = new ArrayList<>(masterData);
        Collections.sort(copy, new Comparator<FoodItem>() {
            @Override public int compare(FoodItem a, FoodItem b) { return Float.compare(b.rating, a.rating); }
        });
        for (int i = 0; i < Math.min(4, copy.size()); i++) nearestData.add(copy.get(i));
        nearestAdapter.notifyDataSetChanged();
    }

    // ---------- Navigation ----------
    private void openProfile() {
        // If you want to require login first, uncomment:
        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // if (user == null) { startActivity(new Intent(this, LoginActivity.class)); return; }

        startActivity(new Intent(MainActivity.this, AccountActivity.class));
    }

    // ---------- Models ----------
    private static class Branch {
        final String name; final double lat, lng;
        Branch(String name, double lat, double lng) { this.name = name; this.lat = lat; this.lng = lng; }
    }
}
