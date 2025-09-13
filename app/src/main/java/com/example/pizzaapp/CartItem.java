package com.example.pizzaapp;

import androidx.annotation.DrawableRes;

public class CartItem {
    public final String title;
    public final String subtitle;
    public final double unitPrice;
    public int qty;
    @DrawableRes public final int imageRes;

    public CartItem(String title, String subtitle, double unitPrice, int qty, @DrawableRes int imageRes) {
        this.title = title;
        this.subtitle = subtitle;
        this.unitPrice = unitPrice;
        this.qty = qty;
        this.imageRes = imageRes;
    }

    public double total() {
        return unitPrice * qty;
    }
}
