package com.foodcash.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.foodcash.adapters.CategoryAdapter;
import com.foodcash.models.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCategory;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private DatabaseReference categoryRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        getSupportActionBar().hide();

        // Ambil ID user yang sedang login
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        categoryRef = FirebaseHelper.getCategoriesReference(userId);

        recyclerViewCategory = findViewById(R.id.recyclerViewCategory);
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this));

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerViewCategory.setAdapter(categoryAdapter);

        // Load data kategori dari Firebase
        loadCategories();

        // Tombol back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Tambahkan tombol untuk kategori baru
        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        btnAddCategory.setOnClickListener(v -> showCategoryDialog(null)); // null untuk kategori baru
    }

    private void loadCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    categoryList.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fungsi untuk menampilkan dialog tambah/edit kategori
    public void showCategoryDialog(Category categoryToEdit) {
        // Inflate layout dialog_form_category.xml
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_form_category, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();

        // Set rounded background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        EditText inputCategoryName = dialogView.findViewById(R.id.inputCategoryName);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        if (categoryToEdit != null) {
            inputCategoryName.setText(categoryToEdit.getCategoryName());
        }

        btnSave.setOnClickListener(v -> {
            String categoryName = inputCategoryName.getText().toString();
            if (!categoryName.isEmpty()) {
                if (categoryToEdit == null) {
                    addCategory(categoryName);
                } else {
                    updateCategory(categoryToEdit.getCategoryId(), categoryName);
                }
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Fungsi untuk menambah kategori baru ke Firebase
    private void addCategory(String categoryName) {
        String categoryId = categoryRef.push().getKey();
        Category category = new Category(categoryId, categoryName);
        categoryRef.child(categoryId).setValue(category)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show());
    }

    // Fungsi untuk mengedit kategori
    private void updateCategory(String categoryId, String categoryName) {
        categoryRef.child(categoryId).child("categoryName").setValue(categoryName)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show());
    }

    // Fungsi untuk menghapus kategori
    public void deleteCategory(String categoryId) {
        categoryRef.child(categoryId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show());
    }
}
