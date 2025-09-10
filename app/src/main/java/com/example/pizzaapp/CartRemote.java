// CartRemote.java
package com.example.pizzaapp;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class CartRemote {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // EXACT SIGNATURE used by the adapter
    public void addToCart(Context ctx,
                          String productId,
                          String name,
                          String imageUrl,
                          double price,
                          int qty) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(ctx, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference ref = db.collection("users")
                .document(user.getUid())
                .collection("cart")
                .document(productId);

        db.runTransaction(t -> {
            DocumentSnapshot snap = t.get(ref);
            long newQty = qty;
            if (snap.exists()) {
                Long q = snap.getLong("qty");
                newQty += (q == null ? 0 : q);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("imageurl", imageUrl == null ? "" : imageUrl);
            data.put("price", price);
            data.put("qty", newQty);
            data.put("updatedAt", FieldValue.serverTimestamp());
            t.set(ref, data, SetOptions.merge());
            return null;
        }).addOnSuccessListener(v ->
                Toast.makeText(ctx, "Added to cart", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(ctx, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }
}
