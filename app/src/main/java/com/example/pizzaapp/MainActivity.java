package com.example.pizzaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import android.app.AlertDialog;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;



import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Views
    private RecyclerView rvPopular, rvNearest;
    private TextView txtNearestBranch, txtSeeAllPopular, emptyView;
    private EditText edtSearch;
    private ImageButton btnFilter, navHome, navHeart, navCart, btnBell;

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
        setContentView(R.layout.activity_dashboard); // your XML

        bindViews();
        setupRecyclerViews();
        seedDemoData();

        // default list
        popularData.clear();
        popularData.addAll(masterData);
        popularAdapter.notifyDataSetChanged();
        toggleEmpty();

        // Search filter
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterPopular(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Clicks
        btnFilter.setOnClickListener(v -> Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show());
        txtSeeAllPopular.setOnClickListener(v -> Toast.makeText(this, "See all popular", Toast.LENGTH_SHORT).show());
        btnBell.setOnClickListener(v -> Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());
        navHome.setOnClickListener(v -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());
        navHeart.setOnClickListener(v -> Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show());
        navCart.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CartActivity.class)));

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
                intent.putExtra(PizzaDetailActivity.EXTRA_PRICE, 1590.0); // TODO: pass real price from DB
                intent.putExtra(PizzaDetailActivity.EXTRA_IMAGE_RES, item.imageRes);
                startActivity(intent);
            }

            @Override public void onLikeClick(FoodItem item) {
                item.liked = !item.liked;
                Toast.makeText(MainActivity.this, (item.liked ? "Added to" : "Removed from") + " favorites", Toast.LENGTH_SHORT).show();
            }
        });

        nearestAdapter = new FoodAdapter(nearestData, new FoodAdapter.OnFoodClickListener() {
            @Override public void onItemClick(FoodItem item) {
                Intent intent = new Intent(MainActivity.this, PizzaDetailActivity.class);
                intent.putExtra(PizzaDetailActivity.EXTRA_TITLE, item.title);
                intent.putExtra(PizzaDetailActivity.EXTRA_SUBTITLE, item.subtitle);
                intent.putExtra(PizzaDetailActivity.EXTRA_RATING, item.rating);
                intent.putExtra(PizzaDetailActivity.EXTRA_PRICE, 1590.0); // TODO: pass real price from DB
                intent.putExtra(PizzaDetailActivity.EXTRA_IMAGE_RES, item.imageRes);
                startActivity(intent);
            }

            @Override public void onLikeClick(FoodItem item) { item.liked = !item.liked; }
        });

        rvPopular.setAdapter(popularAdapter);
        rvNearest.setAdapter(nearestAdapter);
    }

    private void seedDemoData() {
        // Use your existing drawables
        masterData.add(new FoodItem("Margherita", "Classic Pizza", 4.8f, R.drawable.pizza_1));
        masterData.add(new FoodItem("Pepperoni", "Double Cheese", 4.7f, R.drawable.pizza_2));
        masterData.add(new FoodItem("Veggie", "Fresh Garden", 4.6f, R.drawable.pizza_3));
        masterData.add(new FoodItem("BBQ Chicken", "Smoky & Sweet", 4.5f, R.drawable.pizza_4));
        masterData.add(new FoodItem("Hawaiian", "Pineapple Hit", 4.2f, R.drawable.pizza_5));
        masterData.add(new FoodItem("Meat Lovers", "Loaded Feast", 4.9f, R.drawable.pizza_1));
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
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchNearestBranch();
        }
    }

    @SuppressLint("MissingPermission") // we always call this after checking permission
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
        branches.add(new Branch("Galle",   6.0535, 80.2210));
        branches.add(new Branch("Nugegoda",      6.8610, 79.8917));

    }

    private Branch findNearestBranch(Location user) {
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

    // ---------- Models ----------
    private static class Branch {
        final String name; final double lat, lng;
        Branch(String name, double lat, double lng) { this.name = name; this.lat = lat; this.lng = lng; }
    }

    private static class FoodItem {
        final String title, subtitle;
        final float rating;
        @DrawableRes final int imageRes;
        boolean liked = false;
        FoodItem(String title, String subtitle, float rating, @DrawableRes int imageRes) {
            this.title = title; this.subtitle = subtitle; this.rating = rating; this.imageRes = imageRes;
        }
    }

    // ---------- Adapter ----------
    public static class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {

        interface OnFoodClickListener {
            void onItemClick(FoodItem item);
            void onLikeClick(FoodItem item);
        }

        private final List<FoodItem> data;
        private final OnFoodClickListener listener;

        public FoodAdapter(List<FoodItem> data, OnFoodClickListener listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Build a MaterialCard programmatically
            MaterialCardView card = new MaterialCardView(parent.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(dp(parent, 180), ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(parent, 6), dp(parent, 6), dp(parent, 6), dp(parent, 6));
            card.setLayoutParams(lp);
            card.setRadius(dp(parent, 16));
            card.setCardElevation(dp(parent, 4));
            card.setUseCompatPadding(true);

            LinearLayout root = new LinearLayout(parent.getContext());
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(parent, 10), dp(parent, 10), dp(parent, 10), dp(parent, 10));
            card.addView(root);

            ImageView image = new ImageView(parent.getContext());
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(parent, 110));
            image.setLayoutParams(imgLp);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.bg_round_16));
            root.addView(image);

            TextView title = new TextView(parent.getContext());
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tLp.topMargin = dp(parent, 8);
            title.setLayoutParams(tLp);
            root.addView(title);

            TextView subtitle = new TextView(parent.getContext());
            subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            subtitle.setAlpha(0.7f);
            root.addView(subtitle);

            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLp.topMargin = dp(parent, 6);
            row.setLayoutParams(rLp);
            root.addView(row);

            ImageView star = new ImageView(parent.getContext());
            star.setImageResource(R.drawable.ic_star_24);
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(dp(parent, 16), dp(parent, 16));
            star.setLayoutParams(sLp);
            row.addView(star);

            TextView rating = new TextView(parent.getContext());
            rating.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            rating.setAlpha(0.85f);
            rating.setText(" 4.8");
            row.addView(rating);

            Space spacer = new Space(parent.getContext());
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 1, 1f);
            spacer.setLayoutParams(sp);
            row.addView(spacer);

            ImageButton like = new ImageButton(parent.getContext());
            like.setImageResource(R.drawable.ic_heart_outline_24);
            like.setBackground(null);
            LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(dp(parent, 28), dp(parent, 28));
            like.setLayoutParams(lLp);
            row.addView(like);

            return new VH(card, image, title, subtitle, rating, like);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            FoodItem item = data.get(position);
            h.image.setImageResource(item.imageRes);
            h.title.setText(item.title);
            h.subtitle.setText(item.subtitle);
            h.rating.setText(" " + item.rating);
            h.like.setImageResource(item.liked ? android.R.drawable.btn_star_big_on : R.drawable.ic_heart_outline_24);

            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(item); });
            h.like.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClick(item);
                notifyItemChanged(h.getBindingAdapterPosition());
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView title, subtitle, rating;
            final ImageButton like;
            VH(@NonNull View itemView, ImageView image, TextView title, TextView subtitle, TextView rating, ImageButton like) {
                super(itemView);
                this.image = image;
                this.title = title;
                this.subtitle = subtitle;
                this.rating = rating;
                this.like = like;
            }
        }

        private static int dp(ViewGroup parent, int dp) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dp, parent.getResources().getDisplayMetrics());
        }
    }

}
