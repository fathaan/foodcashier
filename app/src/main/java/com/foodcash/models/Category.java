package com.foodcash.models;

public class Category {
    private String categoryId;
    private String categoryName;
    private String name;

    public Category() {
        // Diperlukan untuk Firebase
    }

    public Category(String categoryId, String categoryName) {
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

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
