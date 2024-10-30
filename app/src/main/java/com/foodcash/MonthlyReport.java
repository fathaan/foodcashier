package com.foodcash;

public class MonthlyReport {
    private String month;
    private int total;

    public MonthlyReport(String month, int total) {
        this.month = month;
        this.total = total;
    }

    public String getMonth() { return month; }
    public int getTotal() { return total; }
}
