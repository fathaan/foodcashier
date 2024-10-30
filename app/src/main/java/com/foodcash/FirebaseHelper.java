package com.foodcash;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    private static final String FIREBASE_URL = "https://foodcashier-ee8e3-default-rtdb.firebaseio.com/";

    public static DatabaseReference getUsersReference() {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users");
    }

    public static DatabaseReference getCategoriesReference(String userId) {
        return getUsersReference().child(userId).child("categories");
    }

    public static DatabaseReference getMenusReference(String userId) {
        return getUsersReference().child(userId).child("menus");
    }

    // New method to get Orders reference
    public static DatabaseReference getOrdersReference(String userId) {
        return getUsersReference().child(userId).child("orders");
    }
}
