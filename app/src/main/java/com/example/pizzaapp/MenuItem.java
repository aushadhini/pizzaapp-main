package com.example.pizzaapp;

public class MenuItem {
    public long id;
    public String name;
    public String subtitle;
    public String category;   // e.g., "Pizza", "Burger"
    public double price;      // e.g., 1590.00
    public float rating;      // e.g., 4.8f
    public String imageName;  // drawable name like "pizza_margherita"

    public MenuItem(long id, String name, String subtitle, String category, double price, float rating, String imageName) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.imageName = imageName;
    }
}
