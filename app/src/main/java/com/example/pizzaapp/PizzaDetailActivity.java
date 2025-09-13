package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;

public class PizzaDetailActivity extends AppCompatActivity {

    // Intent extras
    public static final String EXTRA_TITLE      = "extra_title";
    public static final String EXTRA_SUBTITLE   = "extra_subtitle";
    public static final String EXTRA_DESC       = "extra_desc";
    public static final String EXTRA_RATING     = "extra_rating";
    public static final String EXTRA_PRICE      = "extra_price";      // unit price
    public static final String EXTRA_IMAGE_RES  = "extra_image_res";  // int

    private ImageView imgPizza;
    private TextView tvName, tvSubtitle, tvRating, tvDesc, tvPrice, tvQty, tvTotal;
    private View btnPlus, btnMinus;
    private Button btnAddToCart;

    private String title = "", subtitle = "", desc = "";
    private float rating = 0f;
    private double unitPrice = 0.0;
    private int qty = 1;
    @DrawableRes private int imageRes = 0;

    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_detail);

        // Optional toolbar back
        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> onBackPressed());

        bindViews();
        readExtras();
        bindDataToViews();
        wireClicks();
    }

    private void bindViews() {
        imgPizza   = findViewById(R.id.imgPizza);
        tvName     = findViewById(R.id.tvName);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvRating   = findViewById(R.id.tvRating);

        tvPrice    = findViewById(R.id.tvPrice);
        tvQty      = findViewById(R.id.tvQty);
        tvTotal    = findViewById(R.id.tvTotal);    // ok if null (not in XML)

        btnPlus       = findViewById(R.id.btnPlus);
        btnMinus      = findViewById(R.id.btnMinus);
        btnAddToCart  = findViewById(R.id.btnAddToCart);
    }

    private void readExtras() {
        Intent i = getIntent();
        if (i != null) {
            title     = i.getStringExtra(EXTRA_TITLE);
            subtitle  = i.getStringExtra(EXTRA_SUBTITLE);
            desc      = i.getStringExtra(EXTRA_DESC);
            rating    = i.getFloatExtra(EXTRA_RATING, 0f);
            unitPrice = i.getDoubleExtra(EXTRA_PRICE, 0.0);
            imageRes  = i.getIntExtra(EXTRA_IMAGE_RES, R.drawable.ic_image_placeholder);
        }
    }

    private void bindDataToViews() {
        if (tvName != null)     tvName.setText(title != null ? title : "");
        if (tvSubtitle != null) tvSubtitle.setText(subtitle != null ? subtitle : "");
        if (tvRating != null)   tvRating.setText(" " + rating);
        if (tvDesc != null)     tvDesc.setText(desc != null ? desc : "");

        if (imgPizza != null)   imgPizza.setImageResource(imageRes);

        // Show unit price and qty
        if (tvPrice != null) tvPrice.setText("Rs " + money.format(unitPrice));
        if (tvQty != null)   tvQty.setText(String.valueOf(qty));
        updateTotal(); // updates tvTotal if present
    }

    private void wireClicks() {
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                qty++;
                if (tvQty != null) tvQty.setText(String.valueOf(qty));
                updateTotal();
            });
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (qty > 1) {
                    qty--;
                    if (tvQty != null) tvQty.setText(String.valueOf(qty));
                    updateTotal();
                }
            });
        }

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                CartStore.get().addOrIncrement(title, subtitle, unitPrice, qty, imageRes);
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();

                // Optionally jump to the cart right away:
                startActivity(new Intent(PizzaDetailActivity.this, CartActivity.class));
            });

        }
    }

    private void updateTotal() {
        if (tvTotal != null) {
            double total = unitPrice * qty;
            tvTotal.setText("Rs " + money.format(total));
        }
    }
}
