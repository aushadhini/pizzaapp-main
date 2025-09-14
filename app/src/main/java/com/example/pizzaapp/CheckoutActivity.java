package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_OUTLET  = "extra_outlet";
    public static final String EXTRA_TOTAL   = "extra_total";
    public static final String EXTRA_COUNT   = "extra_count";

    private TextView tvAddress, tvOutlet, tvOrderFor, tvTotal, tvItemCount;
    private CheckBox cbTerms;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        tvAddress   = findViewById(R.id.tvAddressSummary);
        tvOutlet    = findViewById(R.id.tvOutlet);
        tvOrderFor  = findViewById(R.id.tvOrderFor);
        tvTotal     = findViewById(R.id.tvTotal);
        tvItemCount = findViewById(R.id.tvItemCount);
        cbTerms     = findViewById(R.id.cbTerms);

        // Title dropdown sample values
        MaterialAutoCompleteTextView etTitle = findViewById(R.id.etTitle);
        etTitle.setSimpleItems(new String[]{"Mr.", "Ms.", "Mrs.", "Dr."});
        etTitle.setText("Mr.", false);

        // Fill from intent
        Intent i = getIntent();
        String address = i.getStringExtra(EXTRA_ADDRESS);
        String outlet  = i.getStringExtra(EXTRA_OUTLET);
        double total   = i.getDoubleExtra(EXTRA_TOTAL, 0);
        int count      = i.getIntExtra(EXTRA_COUNT, 1);

        tvAddress.setText(address == null || address.isEmpty() ? "Delivery to: —" : "Delivery to: " + address);
        tvOutlet.setText(outlet == null || outlet.isEmpty() ? "Outlet : —" : "Outlet : " + outlet);
        tvOrderFor.setText("Now");

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        tvTotal.setText(nf.format(total));
        tvItemCount.setText(String.valueOf(count));

        // Change address
        findViewById(R.id.btnChangeAddress).setOnClickListener(v -> {
            startActivity(new Intent(this, DeliveryActivity.class));
        });

        // Pay now
        MaterialButton pay = findViewById(R.id.btnPayNow);
        pay.setOnClickListener(v -> {
            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Proceeding to payment…", Toast.LENGTH_SHORT).show();
            // TODO: integrate real payment
        });
    }
}
