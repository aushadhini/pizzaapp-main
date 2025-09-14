package com.example.pizzaapp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CartStore {

    private static CartStore INSTANCE;

    public static synchronized CartStore get() {
        if (INSTANCE == null) INSTANCE = new CartStore();
        return INSTANCE;
    }

    private CartStore() {}

    // ---- DATA ----
    private final List<CartItem> items = new ArrayList<>();
    private static final double DELIVERY_FEE = 250.0;   // same as CartActivity
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    // Expose a read-only snapshot
    public List<CartItem> items() {
        return Collections.unmodifiableList(items);
    }

    // Add or increment an item
    public void addOrIncrement(String title, String subtitle, double unitPrice, int qty, int imageRes) {
        for (CartItem c : items) {
            if (c.title.equals(title) && c.unitPrice == unitPrice) {
                c.qty += qty;
                return;
            }
        }
        items.add(new CartItem(title, subtitle, unitPrice, qty, imageRes));
    }

    // Remove first match (used by swipe-to-delete)
    public void removeFirst(String title, double unitPrice) {
        for (int i = 0; i < items.size(); i++) {
            CartItem c = items.get(i);
            if (c.title.equals(title) && c.unitPrice == unitPrice) {
                items.remove(i);
                return;
            }
        }
    }

    public void clear() { items.clear(); }

    // ---- TOTALS ----
    public int totalQuantity() {
        int q = 0;
        for (CartItem c : items) q += c.qty;
        return q;
    }

    public double subtotal() {
        double s = 0.0;
        for (CartItem c : items) s += c.unitPrice * c.qty;
        return s;
    }

    public double deliveryFee() {
        return subtotal() > 0 ? DELIVERY_FEE : 0.0;
    }

    public double totalWithDelivery() {
        return subtotal() + deliveryFee();
    }

    // ---- FORMATTED HELPERS (for UI labels) ----
    public String formattedSubtotal() {
        return "Rs. " + money.format(subtotal());
    }

    public String formattedDeliveryFee() {
        return "Rs. " + money.format(deliveryFee());
    }

    public String formattedTotalWithDelivery() {
        return "Rs. " + money.format(totalWithDelivery());
    }
}
