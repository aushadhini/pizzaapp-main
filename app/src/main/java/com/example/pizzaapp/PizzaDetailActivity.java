package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;

public class PizzaDetailActivity extends AppCompatActivity {

    // Intent extras (use these keys when opening this page)
    public static final String EXTRA_TITLE       = "extra_title";
    public static final String EXTRA_SUBTITLE    = "extra_subtitle";
    public static final String EXTRA_DESC        = "extra_desc";
    public static final String EXTRA_RATING      = "extra_rating";
    public static final String EXTRA_PRICE       = "extra_price";
    public static final String EXTRA_IMAGE_RES   = "extra_image_res";   // int
    public static final String EXTRA_IMAGE_NAME  = "extra_image_name";  // e.g., "pizza_margherita"

    private ImageView imgPizza;
    private TextView tvTitle, tvSubtitle, tvRating, tvDesc, tvPrice, tvQty, tvTotal;
    private Button btnAddToCart;

    private double price = 0.0;
    private int qty = 1;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");
    @DrawableRes private int imageRes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_detail);

        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> onBackPressed());

        imgPizza   = findViewById(R.id.imgPizza);
        tvTitle    = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvRating   = findViewById(R.id.tvRating);
        tvDesc     = findViewById(R.id.tvDesc);
        tvPrice    = findViewById(R.id.tvPrice);
        tvQty      = findViewById(R.id.tvQty);
        tvTotal    = findViewById(R.id.tvTotal);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // ---- Read extras ----
        Intent it = getIntent();
        String title    = it.getStringExtra(EXTRA_TITLE);
        String subtitle = it.getStringExtra(EXTRA_SUBTITLE);
        String desc     = it.getStringExtra(EXTRA_DESC);
        float rating    = it.getFloatExtra(EXTRA_RATING, 4.5f);
        price           = it.getDoubleExtra(EXTRA_PRICE, 1590.0);
        imageRes        = it.getIntExtra(EXTRA_IMAGE_RES, 0);
        String imageName= it.getStringExtra(EXTRA_IMAGE_NAME);

        if (imageRes == 0 && !TextUtils.isEmpty(imageName)) {
            imageRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
        }
        if (imageRes == 0) imageRes = R.drawable.ic_image_placeholder;

        // ---- Bind UI ----
        imgPizza.setImageResource(imageRes);
        tvTitle.setText(title != null ? title : "Pizza");
        tvSubtitle.setText(!TextUtils.isEmpty(subtitle) ? subtitle : "Delicious pizza");
        tvRating.setText(" " + rating);
        tvDesc.setText(!TextUtils.isEmpty(desc)
                ? desc
                : "Freshly baked with quality ingredients. Hot, crispy, and made to order.");
        tvPrice.setText("Rs " + money.format(price));
        tvQty.setText(String.valueOf(qty));
        updateTotal();

        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            qty++;
            tvQty.setText(String.valueOf(qty));
            updateTotal();
        });

        findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (qty > 1) {
                qty--;
                tvQty.setText(String.valueOf(qty));
                updateTotal();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            CartStore.get().addOrIncrement(
                    new CartItem(
                            tvTitle.getText().toString(),
                            tvSubtitle.getText().toString(),
                            price,
                            qty,
                            imageRes
                    )
            );
            startActivity(new Intent(PizzaDetailActivity.this, CartActivity.class));
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });


    }

    private void updateTotal() {
        double total = price * qty;
        tvTotal.setText("Total: Rs " + money.format(total));
    }
}
