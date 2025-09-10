package com.example.pizzaapp;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.VH> {

    public interface OnPizzaClickListener {
        void onItemClicked(Pizza p, int pos);
        // We add to cart directly here, so onAddClicked is optional.
    }

    private final Context ctx;
    private final List<Pizza> data;
    private final OnPizzaClickListener cb;

    public PizzaAdapter(Context ctx, List<Pizza> data, OnPizzaClickListener cb) {
        this.ctx = ctx;
        this.data = data;
        this.cb = cb;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_pizza_grid, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Pizza p = data.get(pos);

        // Image from local drawable resource (int). If you load from URL, swap to Glide here.
        if (p.getImageRes() != 0) {
            h.img.setImageResource(p.getImageRes());
        } else {
            h.img.setImageResource(R.drawable.ic_image_placeholder); // fallback (optional)
        }

        // Name / price (your model currently stores price as String, e.g., "LKR 1550.00")
        h.name.setText(safe(p.getName()));
        h.price.setText(safe(p.getPrice()));

        // Open details on card click (optional)
        h.itemView.setOnClickListener(v -> {
            if (cb != null) cb.onItemClicked(p, pos);
        });

        // Add to Cart → Firestore via CartRemote
        h.btnAdd.setOnClickListener(v -> {
            // Build a stable productId. If you later add a Firestore doc id to Pizza, use that instead.
            String productId = safe(p.getName()); // assumes names are unique in your catalog

            // Parse numeric price from your display string
            double unitPrice = parsePriceToDouble(p.getPrice());

            // Use your CartRemote helper to upsert into users/{uid}/cart/{productId}
            new CartRemote().addToCart(
                    v.getContext(),
                    productId,
                    safe(p.getName()),
                    "",                    // imageUrl: pass "" if you don't have URLs
                    unitPrice,
                    1                      // qty = 1 per tap
            );

            Toast.makeText(v.getContext(),
                    String.format(Locale.getDefault(), "Added %s", safe(p.getName())),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    // ---------- ViewHolder ----------
    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, price;
        MaterialButton btnAdd;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }

    // ---------- Helpers ----------
    private static String safe(String s) { return s == null ? "" : s; }

    /** Extracts a double from strings like "LKR 1,550.00", "Rs. 250.0", "1550" */
    private static double parsePriceToDouble(String priceText) {
        if (TextUtils.isEmpty(priceText)) return 0.0;
        // Keep digits and decimal point only
        String cleaned = priceText.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return 0.0;

        try {
            // If multiple dots somehow appear, keep up to first decimal point
            int firstDot = cleaned.indexOf('.');
            if (firstDot != -1) {
                int nextDot = cleaned.indexOf('.', firstDot + 1);
                if (nextDot != -1) cleaned = cleaned.substring(0, nextDot);
            }
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
