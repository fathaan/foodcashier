package com.foodcash;

// OrderCategory.java
import java.util.List;

public class OrderCategory {
    private String categoryId;
    private String categoryName;
    private List<OrderMenu> menuList; // Add this line

    public OrderCategory() {}

    public OrderCategory(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Add these methods
    public List<OrderMenu> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<OrderMenu> menuList) {
        this.menuList = menuList;
    }
}
