package com.example.pizzaapp;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// NOTE: use the shared types
// Remove any inner CartItem class you had before.
import com.example.pizzaapp.CartItem;
import com.example.pizzaapp.CartStore;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvEmptyCart, tvSubtotal, tvDelivery, tvTotal;
    private Button btnCheckout;

    // shared CartItem list
    private final List<CartItem> cart = new ArrayList<>();
    private CartAdapter adapter;

    private static final double DELIVERY_FEE = 250.0;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> onBackPressed());

        rvCart = findViewById(R.id.rvCart);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cart);
        rvCart.setAdapter(adapter);

        // Load from shared store (works no matter where you opened Cart from)
        cart.clear();
        cart.addAll(CartStore.get().snapshot());
        adapter.notifyDataSetChanged();

        recalcTotals();
        toggleEmpty();

        btnCheckout.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Checkout not implemented (demo)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEmpty() {
        boolean empty = cart.isEmpty();
        tvEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        btnCheckout.setEnabled(!empty);
        btnCheckout.setAlpha(empty ? 0.5f : 1f);
    }

    private void recalcTotals() {
        double subtotal = 0.0;
        for (CartItem c : cart) subtotal += c.price * c.qty;
        double delivery = subtotal > 0 ? DELIVERY_FEE : 0.0;
        double total = subtotal + delivery;

        tvSubtotal.setText("Subtotal: Rs\u00A0" + money.format(subtotal));
        tvDelivery.setText("Delivery: Rs\u00A0" + money.format(delivery));
        tvTotal.setText("Total: Rs\u00A0" + money.format(total));
    }

    // ---------------- Adapter (read-only rows) ----------------
    static class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

        private final List<CartItem> data;
        private final DecimalFormat money = new DecimalFormat("#,##0.00");

        CartAdapter(List<CartItem> data) { this.data = data; }

        @NonNull
        @Override
        public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Card
            MaterialCardView card = new MaterialCardView(parent.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int m = dp(parent, 6);
            lp.setMargins(m, m, m, m);
            card.setLayoutParams(lp);
            card.setRadius(dp(parent, 16));
            card.setCardElevation(dp(parent, 3));
            card.setUseCompatPadding(true);

            // Row
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(parent, 12), dp(parent, 12), dp(parent, 12), dp(parent, 12));
            row.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(row);

            // Image
            ImageView image = new ImageView(parent.getContext());
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dp(parent, 72), dp(parent, 72));
            image.setLayoutParams(imgLp);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.bg_round_16));
            row.addView(image);

            // Texts column
            LinearLayout col = new LinearLayout(parent.getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            colLp.leftMargin = dp(parent, 12);
            col.setLayoutParams(colLp);
            row.addView(col);

            TextView title = new TextView(parent.getContext());
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            title.setMaxLines(1);
            col.addView(title);

            TextView subtitle = new TextView(parent.getContext());
            subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            subtitle.setAlpha(0.7f);
            subtitle.setMaxLines(1);
            col.addView(subtitle);

            LinearLayout bottomRow = new LinearLayout(parent.getContext());
            bottomRow.setOrientation(LinearLayout.HORIZONTAL);
            bottomRow.setGravity(Gravity.CENTER_VERTICAL);
            col.addView(bottomRow);

            TextView price = new TextView(parent.getContext());
            price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            price.setTextColor(0xFF222222);
            LinearLayout.LayoutParams priceLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            price.setLayoutParams(priceLp);
            bottomRow.addView(price);

            TextView qty = new TextView(parent.getContext());
            qty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            qty.setTextColor(0xFF222222);
            qty.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            bottomRow.addView(qty);

            return new CartVH(card, image, title, subtitle, price, qty);
        }

        @Override
        public void onBindViewHolder(@NonNull CartVH h, int position) {
            CartItem item = data.get(position);
            h.image.setImageResource(item.imageRes);
            h.title.setText(item.title);
            h.subtitle.setText(item.subtitle);
            h.price.setText("Rs\u00A0" + money.format(item.price));
            h.qty.setText("Qty: " + item.qty);
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class CartVH extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView title, subtitle, price, qty;
            CartVH(@NonNull View itemView, ImageView image, TextView title, TextView subtitle,
                   TextView price, TextView qty) {
                super(itemView);
                this.image = image; this.title = title; this.subtitle = subtitle;
                this.price = price; this.qty = qty;
            }
        }

        private static int dp(ViewGroup parent, int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                    parent.getResources().getDisplayMetrics());
        }
    }
}
