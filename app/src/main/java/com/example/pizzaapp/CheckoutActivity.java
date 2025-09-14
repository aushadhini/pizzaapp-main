package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    // Intent keys (use these when you start this activity)
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_OUTLET  = "extra_outlet";
    public static final String EXTRA_TOTAL   = "extra_total";
    public static final String EXTRA_COUNT   = "extra_count";

    private static final double DELIVERY_FEE = 250.0;  // match your CartActivity
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private TextView tvAddressSummary, tvOutlet, tvItemCount, tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Optional: Title dropdown choices
        MaterialAutoCompleteTextView etTitle = findViewById(R.id.etTitle);
        if (etTitle != null) {
            etTitle.setSimpleItems(new String[]{"Mr.", "Mrs.", "Ms.", "Dr."});
            etTitle.setText("Mr.", false);
        }

        // -------- 1) Pull data from Intent (if caller sent them) --------
        Intent i = getIntent();
        String addressFromIntent = i != null ? i.getStringExtra(EXTRA_ADDRESS) : null;
        String outletFromIntent  = i != null ? i.getStringExtra(EXTRA_OUTLET)  : null;
        double totalFromIntent   = i != null ? i.getDoubleExtra(EXTRA_TOTAL, Double.NaN) : Double.NaN;
        int    countFromIntent   = i != null ? i.getIntExtra(EXTRA_COUNT, -1)            : -1;

        // -------- 2) Fallbacks (saved prefs + cart) --------
        LocationPrefs lp = new LocationPrefs(this);

        // Address
        String address = addressFromIntent != null ? addressFromIntent : lp.address();
        tvAddressSummary.setText(
                address != null ? "Delivery to: " + address : "Delivery to: —"
        );

        // Outlet
        String outlet = outletFromIntent;
        if (outlet == null) {
            Double lat = lp.lat();
            Double lng = lp.lng();
            if (lat != null && lng != null) {
                Branch nearest = findNearest(lat, lng, BRANCHES);
                outlet = nearest.name;
            }
        }
        tvOutlet.setText("Outlet : " + (outlet != null ? outlet : "—"));

        // Totals (if not provided, compute from CartStore)
        int totalQty = (countFromIntent >= 0) ? countFromIntent : CartStore.get().totalQuantity();
        double total = !Double.isNaN(totalFromIntent) ? totalFromIntent
                : computeTotalWithDelivery();

        tvItemCount.setText(String.valueOf(totalQty));
        tvTotal.setText("Rs. " + MONEY.format(total));
    }

    // ---- Helpers: total with delivery (no dependency on special CartStore methods) ----
    private double computeTotalWithDelivery() {
        double subtotal = 0.0;
        for (CartItem c : CartStore.get().items()) {
            subtotal += c.unitPrice * c.qty;
        }
        return subtotal > 0 ? (subtotal + DELIVERY_FEE) : 0.0;
    }

    // ---- Nearest outlet logic ----
    private static class Branch {
        final String name; final double lat, lng;
        Branch(String n, double la, double ln) { name = n; lat = la; lng = ln; }
    }

    private static final List<Branch> BRANCHES = Arrays.asList(
            new Branch("Nugegoda", 6.8610, 79.8917),
            new Branch("Borella",  6.9157, 79.8648),
            new Branch("Galle",    6.0535, 80.2210),
            new Branch("Kaduwela", 6.9330, 80.0050)
    );

    private Branch findNearest(double uLat, double uLng, List<Branch> list) {
        Branch best = list.get(0);
        double bestD = haversineKm(uLat, uLng, best.lat, best.lng);
        for (int i = 1; i < list.size(); i++) {
            Branch b = list.get(i);
            double d = haversineKm(uLat, uLng, b.lat, b.lng);
            if (d < bestD) { best = b; bestD = d; }
        }
        return best;
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
