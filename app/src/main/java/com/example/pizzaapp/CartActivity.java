package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnChanged {

    private TextView tvSubtotal, tvDelivery, tvTotal;
    private RecyclerView rv;
    private CartAdapter adapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ListenerRegistration cartReg;

    // Local cache of cart items for UI summary
    private final List<CartItem> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_screen);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Views
        rv = findViewById(R.id.rvCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotal = findViewById(R.id.tvTotal);
        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);

        // Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please sign in to view your cart.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(new ArrayList<>(), this);
        rv.setAdapter(adapter);

        // Start listening to Firestore cart
        startCartListener();

        // Checkout click
        btnCheckout.setOnClickListener(v -> {
            if (items.isEmpty()) {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: navigate to Address/Payment screen
                Toast.makeText(this, "Proceeding to checkout…", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(this, CheckoutActivity.class));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCartListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cartReg == null && user != null) startCartListener();
    }

    private void startCartListener() {
        cartReg = db.collection("users").document(user.getUid())
                .collection("cart")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(this, "Cart error: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) return;

                    items.clear();
                    double subtotal = 0;

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String id = d.getId();
                        String name = d.getString("name");
                        String image = d.getString("imageurl");
                        Double price = d.getDouble("price");
                        Long qtyL = d.getLong("qty");

                        double p = price == null ? 0.0 : price;
                        int q = qtyL == null ? 0 : qtyL.intValue();

                        if (q > 0) {
                            CartItem ci = new CartItem(id, name != null ? name : "", image != null ? image : "", p, q);
                            items.add(ci);
                            subtotal += p * q;
                        }
                    }

                    // Update list + totals
                    adapter.submit(new ArrayList<>(items));
                    updateSummary(subtotal);
                });
    }

    private void stopCartListener() {
        if (cartReg != null) {
            cartReg.remove();
            cartReg = null;
        }
    }

    private void updateSummary(double subtotal) {
        double delivery = items.isEmpty() ? 0.0 : 350.0; // flat fee example
        double total = subtotal + delivery;

        tvSubtotal.setText(formatLKR(subtotal));
        tvDelivery.setText(formatLKR(delivery));
        tvTotal.setText(formatLKR(total));
    }

    private String formatLKR(double value) {
        // Locale for Sri Lanka currency formatting
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        return nf.format(value);
    }

    /* ===== CartAdapter.OnChanged callbacks =====
       If your adapter triggers these on + / − / delete,
       we can apply remote updates here as well.
     */
    @Override
    public void onQtyChanged() {
        // No-op: Firestore listener will refresh totals automatically.
        // If you want immediate local feedback, you can recompute from 'items' here.
    }

    @Override
    public void onItemRemoved() {
        // No-op: also handled by Firestore snapshot.
    }

    /* ===== Helper methods to update Firestore from Adapter buttons =====
       Call these from your CartAdapter (onClick of +, −, delete).
     */
    public void setQtyRemote(String productId, int qty) {
        if (user == null) return;
        DocumentReference itemRef = db.collection("users").document(user.getUid())
                .collection("cart").document(productId);
        if (qty <= 0) {
            itemRef.delete();
        } else {
            itemRef.update("qty", qty, "updatedAt", FieldValue.serverTimestamp());
        }
    }

    public void incrementQtyRemote(String productId) {
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .collection("cart").document(productId)
                .update("qty", FieldValue.increment(1), "updatedAt", FieldValue.serverTimestamp());
    }

    public void removeItemRemote(String productId) {
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .collection("cart").document(productId).delete();
    }
}
