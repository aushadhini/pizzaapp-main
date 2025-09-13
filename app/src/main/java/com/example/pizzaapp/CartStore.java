package com.example.pizzaapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory cart store (thread-safe).
 * Works across activities while the app is alive.
 * Swap to Room/SQLite later without changing screen code.
 */
public class CartStore {
    private static final CartStore INSTANCE = new CartStore();
    public static CartStore get() { return INSTANCE; }

    private final List<CartItem> items = new ArrayList<>();

    private CartStore() {}

    /** Add a new item or increment qty if same title + unitPrice already exists. */
    public synchronized void addOrIncrement(String title, String subtitle, double unitPrice, int qty, int imageRes) {
        for (CartItem it : items) {
            if (it.title.equals(title) && it.unitPrice == unitPrice) {
                it.qty += Math.max(1, qty);
                return;
            }
        }
        items.add(new CartItem(title, subtitle, unitPrice, Math.max(1, qty), imageRes));
    }

    /** Update quantity of the first matching item. If qty <= 0, the item is removed. */
    public synchronized void updateQty(String title, double unitPrice, int newQty) {
        for (int i = 0; i < items.size(); i++) {
            CartItem it = items.get(i);
            if (it.title.equals(title) && it.unitPrice == unitPrice) {
                if (newQty <= 0) {
                    items.remove(i);
                } else {
                    it.qty = newQty;
                }
                return;
            }
        }
    }

    /** Remove the first matching item (used by swipe-to-delete). */
    public synchronized void removeFirst(String title, double unitPrice) {
        for (int i = 0; i < items.size(); i++) {
            CartItem it = items.get(i);
            if (it.title.equals(title) && it.unitPrice == unitPrice) {
                items.remove(i);
                return;
            }
        }
    }

    /** Remove by adapter index (optional helper). */
    public synchronized void removeAt(int index) {
        if (index >= 0 && index < items.size()) items.remove(index);
    }

    /** Immutable live view (don’t mutate this list). */
    public synchronized List<CartItem> items() {
        return Collections.unmodifiableList(items);
    }

    /** A mutable copy (handy for adapters that want a local list). */
    public synchronized List<CartItem> snapshot() {
        return new ArrayList<>(items);
    }

    /** Running subtotal of the cart. */
    public synchronized double subtotal() {
        double sub = 0.0;
        for (CartItem it : items) sub += it.total();
        return sub;
    }

    public synchronized int size() { return items.size(); }

    public synchronized boolean isEmpty() { return items.isEmpty(); }

    public synchronized void clear() { items.clear(); }
}
