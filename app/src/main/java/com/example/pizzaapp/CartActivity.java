package com.example.pizzaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart screen with swipe-to-delete, totals and a Checkout hand-off.
 * Expects activity_cart.xml to contain:
 *  - Toolbar (id: toolbar)
 *  - RecyclerView (id: rvCart)
 *  - TextViews: tvEmptyCart, tvSubtotal, tvDelivery, tvTotal
 *  - Button: btnCheckout
 */
public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvEmptyCart, tvSubtotal, tvDelivery, tvTotal;
    private Button btnCheckout;

    private final List<CartItem> cart = new ArrayList<>();
    private CartAdapter adapter;

    private static final double DELIVERY_FEE = 250.0;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    // swipe visuals
    private final ColorDrawable swipeBg = new ColorDrawable(Color.parseColor("#E53935")); // red
    private Drawable deleteIcon; // set in onCreate

    // cached totals
    private double subtotal = 0.0, delivery = 0.0, grandTotal = 0.0;
    private int totalQty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> onBackPressed());

        rvCart      = findViewById(R.id.rvCart);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotal  = findViewById(R.id.tvSubtotal);
        tvDelivery  = findViewById(R.id.tvDelivery);
        tvTotal     = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete_24);
        if (deleteIcon == null) {
            // fallback built-in icon if you don't have ic_delete_24
            deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete);
        }

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cart);
        rvCart.setAdapter(adapter);

        attachSwipeToDelete();

        // initial load
        syncFromStore();

        btnCheckout.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Read saved delivery info (filled when user picked address on map)
            String address = DeliveryPrefs.getAddress(this);       // e.g. "30b Temples Rd, Boralesgamuwa"
            String outlet  = DeliveryPrefs.getNearestOutlet(this); // e.g. "Boralesgamuwa"

            // Launch the checkout page you built earlier
            Intent i = new Intent(this, CheckoutActivity.class);
            i.putExtra(CheckoutActivity.EXTRA_ADDRESS, address);
            i.putExtra(CheckoutActivity.EXTRA_OUTLET,  outlet);
            i.putExtra(CheckoutActivity.EXTRA_TOTAL,   grandTotal);
            i.putExtra(CheckoutActivity.EXTRA_COUNT,   totalQty);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncFromStore();
    }

    private void syncFromStore() {
        cart.clear();
        cart.addAll(CartStore.get().items()); // your backing store
        adapter.notifyDataSetChanged();
        recalcTotals();
        toggleEmpty();
    }

    private void toggleEmpty() {
        boolean empty = cart.isEmpty();
        tvEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        btnCheckout.setEnabled(!empty);
        btnCheckout.setAlpha(empty ? 0.5f : 1f);
    }

    private void recalcTotals() {
        subtotal = 0.0;
        totalQty = 0;
        for (CartItem c : cart) {
            subtotal += c.unitPrice * c.qty;
            totalQty += c.qty;
        }
        delivery   = subtotal > 0 ? DELIVERY_FEE : 0.0;
        grandTotal = subtotal + delivery;

        tvSubtotal.setText("Subtotal: Rs\u00A0" + money.format(subtotal));
        tvDelivery.setText("Delivery: Rs\u00A0" + money.format(delivery));
        tvTotal.setText("Total: Rs\u00A0" + money.format(grandTotal));
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                // remove from adapter + store
                CartItem removed = cart.remove(pos);
                adapter.notifyItemRemoved(pos);
                CartStore.get().removeFirst(removed.title, removed.unitPrice);

                recalcTotals();
                toggleEmpty();

                // Optional: undo
                Snackbar.make(rvCart, "Removed " + removed.title, Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            cart.add(pos, removed);
                            adapter.notifyItemInserted(pos);
                            rvCart.scrollToPosition(pos);
                            CartStore.get().addOrIncrement(
                                    removed.title, removed.subtitle, removed.unitPrice, removed.qty, removed.imageRes
                            );
                            recalcTotals();
                            toggleEmpty();
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = vh.itemView;

                    // draw red bg
                    swipeBg.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    swipeBg.draw(c);

                    // draw trash icon
                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconLeft   = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        int iconRight  = itemView.getRight() - iconMargin;
                        int iconTop    = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(rvCart);
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
            h.price.setText("Rs\u00A0" + money.format(item.unitPrice)); // unit price
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
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dp, parent.getResources().getDisplayMetrics());
        }
    }

    // ---------------- Saved delivery helpers ----------------
    /**
     * Simple prefs helper so we can show address/outlet in checkout.
     * Save into this from DeliveryActivity / MainActivity when user picks a location.
     */
    public static final class DeliveryPrefs {
        private static final String PREFS = "delivery_prefs";
        private static final String KEY_ADDR   = "last_address";
        private static final String KEY_OUTLET = "nearest_outlet";

        public static void save(Context c, String address, String outlet) {
            c.getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_ADDR, address == null ? "" : address)
                    .putString(KEY_OUTLET, outlet == null ? "" : outlet)
                    .apply();
        }

        public static String getAddress(Context c) {
            return c.getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_ADDR, "");
        }

        public static String getNearestOutlet(Context c) {
            return c.getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_OUTLET, "");
        }
    }
}
