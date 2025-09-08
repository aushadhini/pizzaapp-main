package com.example.pizzaapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {

    public interface FoodListener {
        void onAdd(Food f);
        void onFavToggle(Food f, boolean nowFav);
    }

    private final List<Food> items;
    private final FoodListener listener;

    public FoodAdapter(@NonNull List<Food> items, FoodListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_food_card, parent, false); // <-- matches your XML name
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Food f = items.get(pos);

        h.txtTitle.setText(safe(f.name));
        h.txtSubtitle.setText(safe(f.subtitle));
        h.txtPrice.setText(formatRs(f.price)); // supports int/long/double

        if (f.imageurl != null && !f.imageurl.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(f.imageurl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(h.imgFood);
        } else {
            h.imgFood.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Add to cart
        h.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAdd(f);
        });

        // Toggle favorite (simple selected-state UI)
        h.btnFav.setOnClickListener(v -> {
            boolean sel = !v.isSelected();
            v.setSelected(sel);
            if (listener != null) listener.onFavToggle(f, sel);
        });

        // Open details on card tap
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(h.itemView.getContext(), ItemDetailActivity.class);
            i.putExtra("name",      f.name);
            i.putExtra("subtitle",  f.subtitle);
            i.putExtra("desc",      f.desc);
            i.putExtra("price",     f.price);
            i.putExtra("imageurl",  f.imageurl);
            h.itemView.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgFood;
        ImageButton btnFav;
        TextView txtTitle, txtSubtitle, txtPrice;
        MaterialButton btnAdd; // <-- matches MaterialButton in XML

        VH(@NonNull View itemView) {
            super(itemView);
            imgFood     = itemView.findViewById(R.id.imgFood);
            btnFav      = itemView.findViewById(R.id.btnFav);
            txtTitle    = itemView.findViewById(R.id.txtTitle);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);
            txtPrice    = itemView.findViewById(R.id.txtPrice);
            btnAdd      = itemView.findViewById(R.id.btnAdd);
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    // Accepts int/long/double. Formats for Sri Lanka locale.
    private static String formatRs(Object value) {
        double v;
        if (value instanceof Number) {
            v = ((Number) value).doubleValue();
        } else {
            // fallback if price was stored as String
            try { v = Double.parseDouble(String.valueOf(value)); }
            catch (Exception e) { v = 0d; }
        }
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "LK"));
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
        return "Rs. " + nf.format(v);
    }
}
