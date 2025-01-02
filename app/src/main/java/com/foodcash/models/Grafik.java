package com.foodcash.models;

public class Grafik {
    private String menuName;
    private int quantity;

    public Grafik(String menuName, int quantity) {
        this.menuName = menuName;
        this.quantity = quantity;
    }

    public String getMenuName() {
        return menuName;
    }

    public int getQuantity() {
        return quantity;
    }
}