package com.example.pizzaapp;

import androidx.annotation.DrawableRes;

public class FoodItem {
    public final String title;
    public final String subtitle;
    public final float rating;
    public final int reviews;
    @DrawableRes public final int imageRes;
    public boolean liked = false;

    public FoodItem(String title, String subtitle, float rating, @DrawableRes int imageRes, int reviews) {
        this.title = title;
        this.subtitle = subtitle;
        this.rating = rating;
        this.imageRes = imageRes;
        this.reviews = reviews;
    }
}
