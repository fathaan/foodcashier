package com.foodcash.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class OrderDetail {
    private String orderId;
    private String orderDate;
    private String orderTime;
    private List<Item> orderedItems;
    private int totalPrice;

    public OrderDetail(String orderId, String orderDate, String orderTime, List<Item> orderedItems, int totalPrice) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.orderedItems = orderedItems;
        this.totalPrice = totalPrice;
    }
    // Getters and Setters
    public String getOrderId() {return orderId;}
    public void setOrderId(String orderId) {this.orderId = orderId;}
    public String getOrderDate() {return orderDate;}
    public void setOrderDate(String orderDate) {this.orderDate = orderDate;}
    public String getOrderTime() {return orderTime;}
    public void setOrderTime(String orderTime) {this.orderTime = orderTime;}
    public List<Item> getOrderedItems() {return orderedItems;}
    public void setOrderedItems(List<Item> orderedItems) {this.orderedItems = orderedItems;}
    public int getTotalPrice() {return totalPrice;}
    public void setTotalPrice(int totalPrice) {this.totalPrice = totalPrice;}

    // Inner class to represent each item in the order
    public static class Item implements Parcelable {
        private String menuId;
        private String menuName;
        private int menuPrice;
        private int quantity;

        public Item(String menuId, String menuName, int menuPrice, int quantity) {
            this.menuId = menuId;
            this.menuName = menuName;
            this.menuPrice = menuPrice;
            this.quantity = quantity;
        }

        protected Item(Parcel in) {
            menuId = in.readString();
            menuName = in.readString();
            menuPrice = in.readInt();
            quantity = in.readInt();
        }
        public static final Creator<Item> CREATOR = new Creator<Item>() {
            @Override
            public Item createFromParcel(Parcel in) {return new Item(in); }
            @Override
            public Item[] newArray(int size) {return new Item[size];}
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(menuId);
            dest.writeString(menuName);
            dest.writeInt(menuPrice);
            dest.writeInt(quantity);
        }
        @Override
        public int describeContents() {return 0;}
        // Getters and Setters for Item class
        public String getMenuId() {return menuId;}
        public void setMenuId(String menuId) {this.menuId = menuId;}
        public String getMenuName() {return menuName;}
        public void setMenuName(String menuName) {this.menuName = menuName;}
        public int getMenuPrice() {return menuPrice;}
        public void setMenuPrice(int menuPrice) {this.menuPrice = menuPrice;}
        public int getQuantity() {return quantity;}
        public void setQuantity(int quantity) {this.quantity = quantity;}
    }
}
