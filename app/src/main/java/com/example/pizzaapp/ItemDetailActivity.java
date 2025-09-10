package com.example.pizzaapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtPrice, txtQty, btnMinus, btnPlus; // TextView types match XML
    private MaterialButton btnAddToCart;
    private ImageView imgHero;

    private int qty = 1;           // default quantity
    private Pizza pizza;           // passed via Intent ("pizza") and should implement Serializable/Parcelable

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Bind views (ids must match activity_item_detail.xml)
        imgHero     = findViewById(R.id.imgHero);
        txtTitle    = findViewById(R.id.txtTitle);
        txtPrice    = findViewById(R.id.txtPrice);
        txtQty      = findViewById(R.id.txtQty);     // <-- quantity label
        btnMinus    = findViewById(R.id.btnMinus);   // <-- minus TextView
        btnPlus     = findViewById(R.id.btnPlus);    // <-- plus TextView
        btnAddToCart= findViewById(R.id.btnAddToCart);

        // Get pizza from Intent
        Object extra = getIntent().getSerializableExtra("pizza");
        if (extra instanceof Pizza) {
            pizza = (Pizza) extra;
        }

        if (pizza != null) {
            txtTitle.setText(pizza.getName());
            txtPrice.setText(pizza.getPrice());
            // If you have a drawable resource id:
            if (pizza.getImageRes() != 0) {
                imgHero.setImageResource(pizza.getImageRes());
            }
            // If you use URLs, load with Glide/Picasso instead.
        }

        // Default qty
        txtQty.setText(String.valueOf(qty));

        // Minus
        btnMinus.setOnClickListener(v -> {
            if (qty > 1) {
                qty--;
                txtQty.setText(String.valueOf(qty));
            }
        });

        // Plus
        btnPlus.setOnClickListener(v -> {
            qty++;
            txtQty.setText(String.valueOf(qty));
        });

        // Add to cart (local CartManager – replace with CartRemote if you want Firestore)
        btnAddToCart.setOnClickListener(v -> {
            if (pizza == null) return;

            String id = pizza.getName(); // use a real doc id if you have one
            double priceDouble = parsePriceToDouble(pizza.getPrice());

            CartItem item = new CartItem(
                    id,
                    pizza.getName(),
                    "",                 // imageUrl if you have one, else ""
                    priceDouble,
                    qty
            );

            CartManager.get(this).addItem(this, item);
            Toast.makeText(this, qty + " × " + pizza.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    private double parsePriceToDouble(String priceText) {
        if (priceText == null) return 0.0;
        String cleaned = priceText.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
