package com.example.pizzaapp;

public class CartItem {
    private String id;        // productId
    private String name;
    private String imageUrl;  // or drawable name
    private double price;
    private int qty;

    public CartItem(String id, String name, String imageUrl, double price, int qty) {
        this.id = id; this.name = name; this.imageUrl = imageUrl; this.price = price; this.qty = qty;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double lineTotal() { return price * qty; }
}
