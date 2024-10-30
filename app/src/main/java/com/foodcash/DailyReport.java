package com.foodcash;

public class DailyReport {
    private String date;
    private int total;
    private int totalCustomers;

    public DailyReport(String date, int total, int totalCustomers) {
        this.date = date;
        this.total = total;
        this.totalCustomers = totalCustomers;
    }

    public String getDate() { return date; }
    public int getTotal() { return total; }
    public int getTotalCustomers() { return totalCustomers; }

    public void incrementTotalPrice(int price) {
        this.total += price;
    }

    public void incrementTotalCustomers() {
        this.totalCustomers++;
    }
}
