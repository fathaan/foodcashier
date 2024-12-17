package com.foodcash.models;

import java.util.List;

// Order.java
public class Order {
    private String orderId;
    private String userId;
    private List<OrderMenu> items;
    private int totalPrice;
    private long timestamp;

    public Order() {
        // Diperlukan untuk Firebase
    }

    public Order(String orderId, String userId, List<OrderMenu> items, int totalPrice, long timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderMenu> getItems() {
        return items;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
