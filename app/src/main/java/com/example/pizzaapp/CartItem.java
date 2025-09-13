package com.example.pizzaapp;
import com.example.pizzaapp.CartItem;

import androidx.annotation.DrawableRes;

public class CartItem {
    public final String title, subtitle;
    public final double price;
    public int qty;
    @DrawableRes public final int imageRes;

    public CartItem(String t, String s, double p, int q, @DrawableRes int res) {
        title = t; subtitle = s; price = p; qty = q; imageRes = res;
    }
}
