package com.example.etradee;

import java.math.BigDecimal;

public class Product {

    private int id;
    private String name;
    private double price;
    private int quantity;
    private String quality;


    public Product(int id, String name, double price, int quantity, String quality) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.quality = quality;
    }

    // Getters and setters (generated or manually implemented)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getitem_purchased() {
        return null;
    }

    public BigDecimal getTotal() {
        return null;
    }
}
