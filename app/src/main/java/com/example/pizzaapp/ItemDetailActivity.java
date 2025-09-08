package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

public class ItemDetailActivity extends AppCompatActivity {

    private ImageView imgHero;
    private TextView txtTitle, txtPrice, txtSubtitle, txtDesc, txtQty;
    private TextView btnMinus, btnPlus;
    private MaterialButton btnAddToCart;

    private int quantity = 1;
    private long priceValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Bind views
        imgHero = findViewById(R.id.imgHero);
        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        txtDesc = findViewById(R.id.txtDesc);
        txtQty = findViewById(R.id.txtQty);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // Get data from intent
        Intent i = getIntent();
        String name = i.getStringExtra("name");
        String subtitle = i.getStringExtra("subtitle");
        String desc = i.getStringExtra("desc");
        String imageUrl = i.getStringExtra("imageurl");
        priceValue = i.getLongExtra("price", 0);

        // Set UI
        txtTitle.setText(name);
        txtSubtitle.setText(subtitle);
        txtDesc.setText(desc != null ? desc : "");
        txtPrice.setText("Rs. " + priceValue);
        txtQty.setText(String.valueOf(quantity));

        // Load image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgHero);
        } else {
            imgHero.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Quantity buttons
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQty.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            txtQty.setText(String.valueOf(quantity));
        });

        // Add to cart button
        btnAddToCart.setOnClickListener(v -> {
            // For now just show a toast. Later you can save to Firestore/Realtime DB
            String msg = quantity + " × " + name + " added to cart";
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
        });
    }
}
