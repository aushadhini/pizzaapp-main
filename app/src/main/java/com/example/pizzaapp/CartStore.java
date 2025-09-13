package com.example.pizzaapp;

import java.util.ArrayList;
import java.util.List;

public class CartStore {
    private static CartStore INSTANCE;
    private final List<CartItem> items = new ArrayList<>();

    private CartStore() {}

    public static synchronized CartStore get() {
        if (INSTANCE == null) INSTANCE = new CartStore();
        return INSTANCE;
    }

    /** Return a copy so callers can't mutate internal list directly */
    public synchronized List<CartItem> snapshot() {
        return new ArrayList<>(items);
    }

    /** Merge by title; bump qty if same title already exists */
    public synchronized void addOrIncrement(CartItem newItem) {
        for (CartItem it : items) {
            if (it.title.equals(newItem.title)) {
                it.qty += newItem.qty;
                return;
            }
        }
        items.add(newItem);
    }

    public synchronized void clear() { items.clear(); }

    public synchronized double subtotal() {
        double s = 0.0;
        for (CartItem c : items) s += c.price * c.qty;
        return s;
    }
}
