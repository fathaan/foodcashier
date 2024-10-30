package com.foodcash;

public class Menu {
    private String menuId;
    private String menuName;
    private String categoryName;
    private int menuPrice;

    public Menu(){}

    // Constructor, getter, and setter
    public Menu(String menuId, String menuName, String categoryName, int menuPrice) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.categoryName = categoryName;
        this.menuPrice = menuPrice;
    }

    public String getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public String getCategoryName() { return categoryName; }
    public int getMenuPrice() { return menuPrice; }
}
