package com.example.pizzaapp;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
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

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvEmptyCart, tvSubtotal, tvDelivery, tvTotal;
    private Button btnCheckout;

    private final List<CartItem> cart = new ArrayList<>();
    private CartAdapter adapter;

    private static final double DELIVERY_FEE = 250.0;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Toolbar back
        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> onBackPressed());

        rvCart = findViewById(R.id.rvCart);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cart, new CartAdapter.Listener() {
            @Override public void onQtyChanged() { recalcTotals(); toggleEmpty(); }
            @Override public void onRemoved()    { recalcTotals(); toggleEmpty(); }
        });
        rvCart.setAdapter(adapter);

        if (getIntent() != null && getIntent().hasExtra("cart_title")) {
            String t = getIntent().getStringExtra("cart_title");
            String s = getIntent().getStringExtra("cart_subtitle");
            double p = getIntent().getDoubleExtra("cart_price", 0);
            int q    = getIntent().getIntExtra("cart_qty", 1);
            int res  = getIntent().getIntExtra("cart_image_res", R.drawable.ic_image_placeholder);
            cart.add(new CartActivity.CartItem(t, s, p, q, res));
            adapter.notifyItemInserted(cart.size() - 1);
        }

        // Demo items — replace with your real Add-to-Cart flow
        seedDemo();

        recalcTotals();
        toggleEmpty();

        btnCheckout.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Checkout not implemented (demo)", Toast.LENGTH_SHORT).show();
        });
    }

    private void seedDemo() {
        cart.clear();
        cart.add(new CartItem("Margherita", "Classic Pizza", 1590.0, 1, R.drawable.ic_image_placeholder));
        cart.add(new CartItem("Pepperoni", "Double Cheese", 1790.0, 2, R.drawable.ic_image_placeholder));
        adapter.notifyDataSetChanged();
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

        tvSubtotal.setText("Subtotal: Rs " + money.format(subtotal));
        tvDelivery.setText("Delivery: Rs " + money.format(delivery));
        tvTotal.setText("Total: Rs " + money.format(total));
    }

    // ---------------- Models ----------------
    static class CartItem {
        final String title, subtitle;
        final double price;
        int qty;
        @DrawableRes final int imageRes;
        CartItem(String t, String s, double p, int q, @DrawableRes int res) {
            title=t; subtitle=s; price=p; qty=q; imageRes=res;
        }
    }

    // --------------- Adapter ----------------
    static class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

        interface Listener { void onQtyChanged(); void onRemoved(); }

        private final List<CartItem> data;
        private final Listener listener;
        private final DecimalFormat money = new DecimalFormat("#,##0.00");

        CartAdapter(List<CartItem> data, Listener l) {
            this.data = data;
            this.listener = l;
        }

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

            // Texts
            LinearLayout col = new LinearLayout(parent.getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            colLp.leftMargin = dp(parent, 12);
            col.setLayoutParams(colLp);
            row.addView(col);

            TextView title = new TextView(parent.getContext());
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            col.addView(title);

            TextView subtitle = new TextView(parent.getContext());
            subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            subtitle.setAlpha(0.7f);
            col.addView(subtitle);

            TextView price = new TextView(parent.getContext());
            price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            price.setTextColor(0xFF222222);
            col.addView(price);

            // Qty controls
            LinearLayout qtyRow = new LinearLayout(parent.getContext());
            qtyRow.setOrientation(LinearLayout.HORIZONTAL);
            qtyRow.setGravity(Gravity.CENTER);
            row.addView(qtyRow);

            TextView btnMinus = pillButton(parent, "−");
            TextView tvQty = new TextView(parent.getContext());
            LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(dp(parent, 40), dp(parent, 36));
            qlp.leftMargin = dp(parent, 6);
            qlp.rightMargin = dp(parent, 6);
            tvQty.setLayoutParams(qlp);
            tvQty.setGravity(Gravity.CENTER);
            tvQty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvQty.setText("1");
            TextView btnPlus = pillButton(parent, "+");

            qtyRow.addView(btnMinus); qtyRow.addView(tvQty); qtyRow.addView(btnPlus);

            return new CartVH(card, image, title, subtitle, price, btnMinus, tvQty, btnPlus);
        }

        private static TextView pillButton(ViewGroup parent, String text) {
            TextView tv = new TextView(parent.getContext());
            tv.setText(text);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tv.setTextColor(0xFF000000);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(parent, 36), dp(parent, 36));
            tv.setLayoutParams(lp);
            tv.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.bg_round_16));
            tv.setClickable(true);
            tv.setFocusable(true);
            return tv;
        }

        @Override
        public void onBindViewHolder(@NonNull CartVH h, int position) {
            CartItem item = data.get(position);

            h.image.setImageResource(item.imageRes);
            h.title.setText(item.title);
            h.subtitle.setText(item.subtitle);
            h.price.setText("Rs " + money.format(item.price));
            h.qtyText.setText(String.valueOf(item.qty));

            h.btnPlus.setOnClickListener(v -> {
                item.qty++;
                h.qtyText.setText(String.valueOf(item.qty));
                if (listener != null) listener.onQtyChanged();
            });

            h.btnMinus.setOnClickListener(v -> {
                if (item.qty > 1) {
                    item.qty--;
                    h.qtyText.setText(String.valueOf(item.qty));
                    if (listener != null) listener.onQtyChanged();
                } else {
                    int idx = h.getBindingAdapterPosition();
                    if (idx != RecyclerView.NO_POSITION) {
                        data.remove(idx);
                        notifyItemRemoved(idx);
                        if (listener != null) listener.onRemoved();
                    }
                }
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        // ---- ViewHolder (matches the generics!) ----
        static class CartVH extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView title, subtitle, price;
            final TextView btnMinus, qtyText, btnPlus;
            CartVH(@NonNull View itemView, ImageView image, TextView title, TextView subtitle,
                   TextView price, TextView btnMinus, TextView qtyText, TextView btnPlus) {
                super(itemView);
                this.image = image; this.title = title; this.subtitle = subtitle;
                this.price = price; this.btnMinus = btnMinus; this.qtyText = qtyText; this.btnPlus = btnPlus;
            }
        }

        private static int dp(ViewGroup parent, int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                    parent.getResources().getDisplayMetrics());
        }
    }
}
