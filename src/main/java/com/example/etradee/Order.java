package com.example.etradee;

public class Order {
    private final String accountName;
    private final String time;
    private final String date;
    private final String itemPurchased;
    private final String quantity;
    private final String price;
    private final String transactionNumber;
    private final String transactionDetails;
    private  String quality;

    public Order(String accountName, String time, String date, String itemPurchased, String quantity, String price, String transactionNumber, String transactionDetails, String quality) {
        this.accountName = accountName;
        this.time = time;
        this.date = date;
        this.itemPurchased = itemPurchased;
        this.quantity = quantity;
        this.price = price;
        this.transactionNumber = transactionNumber;
        this.transactionDetails = transactionDetails;
        this.quality = quality;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getItemPurchased() {
        return itemPurchased;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public String getQuality() {
        return quality;
    }
}
