package com.foodcash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.CartListener;
import com.foodcash.models.OrderCategory;
import com.foodcash.R;
import com.foodcash.adapters.OrderCategoryAdapter;
import com.foodcash.models.OrderDetail;
import com.foodcash.models.OrderMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity implements CartListener {

    private RecyclerView itemCategoryRecycler;
    private TextView cartTotal;
    private Button btnDetailOrder;
    private List<OrderCategory> categoryList = new ArrayList<>();
    private int totalAmount = 0;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        getSupportActionBar().hide();

        itemCategoryRecycler = findViewById(R.id.itemCategorydanMenu);
        cartTotal = findViewById(R.id.cartTotal);
        btnDetailOrder = findViewById(R.id.btnDetailOrder);

        itemCategoryRecycler.setLayoutManager(new LinearLayoutManager(this));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        loadCategoriesAndMenus();

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        btnDetailOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderActivity.this, OrderDetailActivity.class);

            ArrayList<OrderDetail.Item> selectedItems = new ArrayList<>();
            for (OrderCategory category : categoryList) {
                for (OrderMenu menu : category.getMenuList()) {
                    if (menu.getQuantity() > 0) {
                        selectedItems.add(new OrderDetail.Item(menu.getMenuId(), menu.getMenuName(), menu.getMenuPrice(), menu.getQuantity()));
                    }
                }
            }

            intent.putParcelableArrayListExtra("selectedItems", selectedItems);
            intent.putExtra("totalAmount", totalAmount);
            startActivity(intent);
        });
    }

    private void loadCategoriesAndMenus() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();

                for (DataSnapshot categorySnapshot : snapshot.child("categories").getChildren()) {
                    String categoryId = categorySnapshot.getKey();
                    String categoryName = categorySnapshot.child("categoryName").getValue(String.class);

                    OrderCategory category = new OrderCategory(categoryId, categoryName);

                    List<OrderMenu> menuList = new ArrayList<>();
                    for (DataSnapshot menuSnapshot : snapshot.child("menus").getChildren()) {
                        String menuCategory = menuSnapshot.child("categoryName").getValue(String.class);

                        if (menuCategory != null && menuCategory.equals(categoryName)) {
                            String menuId = menuSnapshot.child("menuId").getValue(String.class);
                            String menuName = menuSnapshot.child("menuName").getValue(String.class);
                            Long menuPriceLong = menuSnapshot.child("menuPrice").getValue(Long.class);
                            int menuPrice = (menuPriceLong != null) ? menuPriceLong.intValue() : 0;

                            OrderMenu menu = new OrderMenu(menuId, menuName, menuPrice);
                            menuList.add(menu);
                        }
                    }
                    category.setMenuList(menuList);
                    categoryList.add(category);
                }

                OrderCategoryAdapter adapter = new OrderCategoryAdapter(OrderActivity.this, categoryList, OrderActivity.this);
                itemCategoryRecycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderActivity", "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onCartUpdated() {
        totalAmount = 0;
        for (OrderCategory category : categoryList) {
            for (OrderMenu menu : category.getMenuList()) {
                totalAmount += menu.getQuantity() * menu.getMenuPrice();
            }
        }
        cartTotal.setText(formatToRupiah(totalAmount));
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }
}
