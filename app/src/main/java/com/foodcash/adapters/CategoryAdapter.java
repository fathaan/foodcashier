package com.foodcash.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.activity.CategoryActivity;
import com.foodcash.R;
import com.foodcash.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private CategoryActivity activity; // Reference to CategoryActivity

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.activity = activity; // Initialize activity reference
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryTextView.setText(category.getCategoryName());

        // Handle button action click to show dialog
        holder.btnAction.setOnClickListener(v -> showActionDialog(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryTextView;
        public ImageButton btnAction;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            btnAction = itemView.findViewById(R.id.btnActionCategory);
        }
    }

    private void showActionDialog(Category category) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_action_category, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();

        // Set rounded background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
            ((CategoryActivity) context).showCategoryDialog(category); // Panggil fungsi edit di activity
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            ((CategoryActivity) context).deleteCategory(category.getCategoryId()); // Panggil fungsi hapus di activity
            dialog.dismiss();
        });

        dialog.show();
    }

}
