package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    // Optional extras if you prefer to pass data in the Intent
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_OUTLET  = "extra_outlet";
    public static final String EXTRA_TOTAL   = "extra_total";
    public static final String EXTRA_COUNT   = "extra_count";

    // Delivery fee; keep in sync with your Cart screen
    private static final double DELIVERY_FEE = 250.0;
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private TextView tvAddressSummary, tvOutlet, tvItemCount, tvTotal, btnChangeAddress;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Toolbar back
        MaterialToolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> onBackPressed());

        // Views
        tvAddressSummary = findViewById(R.id.tvAddressSummary);
        tvOutlet         = findViewById(R.id.tvOutlet);
        tvItemCount      = findViewById(R.id.tvItemCount);
        tvTotal          = findViewById(R.id.tvTotal);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        // Title drop-down (Mr/Mrs/Ms/Dr)
        MaterialAutoCompleteTextView etTitle = findViewById(R.id.etTitle);
        if (etTitle != null) {
            etTitle.setSimpleItems(new String[]{"Mr.", "Mrs.", "Ms.", "Dr."});
            etTitle.setText("Mr.", false);
        }

        // Tap "Change" to pick address again
        if (btnChangeAddress != null) {
            btnChangeAddress.setOnClickListener(v ->
                    startActivity(new Intent(CheckoutActivity.this, DeliveryActivity.class)));
        }

        refreshUiFromPrefsOrIntent();
    }

    @Override protected void onResume() {
        super.onResume();
        // If the user picked a new location, update labels
        refreshUiFromPrefsOrIntent();
    }

    /** Reads Intent extras (if any), otherwise falls back to LocationPrefs + CartStore. */
    private void refreshUiFromPrefsOrIntent() {
        Intent i = getIntent();

        // ---- Address
        String address = (i != null) ? i.getStringExtra(EXTRA_ADDRESS) : null;
        if (address == null) address = new LocationPrefs(this).address();
        tvAddressSummary.setText(address != null ? "Delivery to: " + address : "Delivery to: —");

        // ---- Outlet (nearest by saved lat/lng if not provided)
        String outlet = (i != null) ? i.getStringExtra(EXTRA_OUTLET) : null;
        if (outlet == null) {
            LocationPrefs lp = new LocationPrefs(this);
            Double lat = lp.lat();
            Double lng = lp.lng();
            if (lat != null && lng != null) {
                Branch b = findNearest(lat, lng, BRANCHES);
                outlet = b.name;
            }
        }
        tvOutlet.setText("Outlet : " + (outlet != null ? outlet : "—"));

        // ---- Item count
        int count = (i != null) ? i.getIntExtra(EXTRA_COUNT, -1) : -1;
        if (count < 0) count = totalQuantityFromCart();
        tvItemCount.setText(String.valueOf(count));

        // ---- Total (fallback: compute from cart + delivery)
        double total = (i != null) ? i.getDoubleExtra(EXTRA_TOTAL, Double.NaN) : Double.NaN;
        if (Double.isNaN(total)) total = computeTotalWithDelivery();
        tvTotal.setText("Rs. " + MONEY.format(total));
    }

    // ---------- Cart helpers ----------
    private int totalQuantityFromCart() {
        int qty = 0;
        for (CartItem c : CartStore.get().items()) qty += c.qty;
        return qty;
    }

    private double computeTotalWithDelivery() {
        double subtotal = 0.0;
        for (CartItem c : CartStore.get().items()) {
            subtotal += c.unitPrice * c.qty;
        }
        return subtotal > 0 ? (subtotal + DELIVERY_FEE) : 0.0;
    }

    // ---------- Nearest outlet ----------
    private static class Branch {
        final String name; final double lat, lng;
        Branch(String n, double la, double ln) { name = n; lat = la; lng = ln; }
    }

    // Add/adjust outlets as you need
    private static final List<Branch> BRANCHES = Arrays.asList(
            new Branch("Nugegoda", 6.8610, 79.8917),
            new Branch("Borella",  6.9157, 79.8648),
            new Branch("Galle",    6.0535, 80.2210),
            new Branch("Kaduwela", 6.9330, 80.0050)
    );

    private Branch findNearest(double uLat, double uLng, List<Branch> list) {
        Branch best = list.get(0);
        double bestD = haversineKm(uLat, uLng, best.lat, best.lng);
        for (int idx = 1; idx < list.size(); idx++) {
            Branch b = list.get(idx);
            double d = haversineKm(uLat, uLng, b.lat, b.lng);
            if (d < bestD) { best = b; bestD = d; }
        }
        return best;
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
