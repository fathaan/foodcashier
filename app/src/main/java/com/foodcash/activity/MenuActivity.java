package com.foodcash.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.foodcash.adapters.MenuAdapter;
import com.foodcash.models.Menu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private MenuAdapter menuAdapter;
    private List<Menu> menuList;
    private DatabaseReference menusRef;
    private String userId;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        menusRef = FirebaseHelper.getMenusReference(userId);

        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        menuList = new ArrayList<>();
        menuAdapter = new MenuAdapter(menuList);
        recyclerViewMenu.setAdapter(menuAdapter);

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());

        findViewById(R.id.btnAddMenu).setOnClickListener(view -> showAddMenuDialog());

        loadMenus();
    }

    private void loadMenus() {
        menusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Menu menu = data.getValue(Menu.class);
                    menuList.add(menu);
                }
                menuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuActivity.this, "Failed to load menus", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddMenuDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_form_menu, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Atur background dari dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText inputMenuName = dialogView.findViewById(R.id.inputMenuName);
        EditText inputPrice = dialogView.findViewById(R.id.inputPrice);

        loadCategories(spinnerCategory);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(view -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(view -> {
            String menuName = inputMenuName.getText().toString();
            String categoryName = spinnerCategory.getSelectedItem().toString();
            int price = Integer.parseInt(inputPrice.getText().toString());
            addMenu(menuName, categoryName, price);
            dialog.dismiss();
        });

        dialog.show();
    }
    public void loadCategories( Spinner spinnerCategory) {
        DatabaseReference categoriesRef = FirebaseHelper.getCategoriesReference(userId);
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryName = categorySnapshot.child("categoryName").getValue(String.class);
                    if (categoryName != null) {
                        categories.add(categoryName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, categories);
                spinnerCategory.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addMenu(String menuName, String categoryName, int price) {
        String menuId = menusRef.push().getKey();
        Menu menu = new Menu(menuId, menuName, categoryName, price);
        menusRef.child(menuId).setValue(menu).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MenuActivity.this, "Menu added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MenuActivity.this, "Failed to add menu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
