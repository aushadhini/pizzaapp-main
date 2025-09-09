package com.example.pizzaapp;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class CartManager {
    private static CartManager INSTANCE;
    private final Map<String, CartItem> items = new LinkedHashMap<>();
    private static final String SP_NAME = "cart_sp";
    private static final String KEY_CART = "cart_json";
    private final SharedPreferences sp;

    private CartManager(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized CartManager get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new CartManager(ctx);
        return INSTANCE;
    }

    public void addItem(Context ctx, CartItem item) {
        CartItem existing = items.get(item.getId());
        if (existing == null) items.put(item.getId(), item);
        else existing.setQty(existing.getQty() + item.getQty());
        save();
    }

    public void setQty(String id, int qty) {
        CartItem it = items.get(id);
        if (it == null) return;
        if (qty <= 0) items.remove(id);
        else it.setQty(qty);
        save();
    }

    public void remove(String id) { items.remove(id); save(); }
    public void clear() { items.clear(); save(); }

    public List<CartItem> getItems() { return new ArrayList<>(items.values()); }
    public double getSubtotal() {
        double s = 0;
        for (CartItem ci : items.values()) s += ci.lineTotal();
        return s;
    }
    public double getDelivery() { return items.isEmpty() ? 0 : 350.0; } // example flat fee
    public double getTotal() { return getSubtotal() + getDelivery(); }

    private void save() {
        try {
            JSONArray arr = new JSONArray();
            for (CartItem c : items.values()) {
                JSONObject o = new JSONObject();
                o.put("id", c.getId());
                o.put("name", c.getName());
                o.put("imageUrl", c.getImageUrl());
                o.put("price", c.getPrice());
                o.put("qty", c.getQty());
                arr.put(o);
            }
            sp.edit().putString(KEY_CART, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void load() {
        items.clear();
        try {
            String raw = sp.getString(KEY_CART, "[]");
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                CartItem c = new CartItem(
                        o.getString("id"),
                        o.getString("name"),
                        o.optString("imageUrl", ""),
                        o.getDouble("price"),
                        o.getInt("qty")
                );
                items.put(c.getId(), c);
            }
        } catch (Exception ignored) {}
    }
}
