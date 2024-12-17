package com.foodcash.models;

public class History {
    private String amount;    // Formatted price in Rupiah
    private String time;      // Order time
    private String transactionId;  // Order ID

    public History() {
        // Default constructor required for calls to DataSnapshot.getValue(History.class)
    }

    public History(String amount, String time, String transactionId) {
        this.amount = amount;
        this.time = time;
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
