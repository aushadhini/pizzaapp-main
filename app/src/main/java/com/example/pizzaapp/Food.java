package com.example.pizzaapp;

public class Food {
    public String name;       // e.g. "BBQ Chicken"
    public String subtitle;   // e.g. "Pizza"
    public String desc;       // long description
    public String imageurl;   // http/https image url
    public long   price;      // e.g. 1600

    public Food() {} // Firestore/JSON needs empty ctor

    public Food(String name, String subtitle, String desc, String imageurl, long price) {
        this.name = name;
        this.subtitle = subtitle;
        this.desc = desc;
        this.imageurl = imageurl;
        this.price = price;
    }
}
