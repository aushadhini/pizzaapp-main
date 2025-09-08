package com.example.pizzaapp;

public class Pizza {
    private final int imageRes;
    private final String name;
    private final String price;

    public Pizza(int imageRes, String name, String price) {
        this.imageRes = imageRes; this.name = name; this.price = price;
    }
    public int getImageRes() { return imageRes; }
    public String getName()  { return name; }
    public String getPrice() { return price; }
}
