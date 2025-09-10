// Pizza.java
package com.example.pizzaapp;

public class Pizza {
    private String id;        // NEW field
    private String name;
    private String price;
    private int imageRes;
    private String imageUrl;  // optional, if you load from Firestore

    public Pizza() {} // Firestore/adapter needs empty constructor

    public Pizza(String id, String name, String price, int imageRes, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
    }

    // Getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageRes() { return imageRes; }
    public String getImageUrl() { return imageUrl; }
}
