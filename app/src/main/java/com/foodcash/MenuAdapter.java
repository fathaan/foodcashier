package com.foodcash;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<Menu> menuList;

    public MenuAdapter(List<Menu> menuList) {
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu menu = menuList.get(position);
        holder.menuNameTextView.setText(menu.getMenuName());
        holder.categoryNameTextView.setText(menu.getCategoryName());

        holder.btnActionMenu.setOnClickListener(v -> showActionMenuDialog(holder.itemView.getContext(), menu));
        holder.priceTextView.setText(formatRupiah(menu.getMenuPrice()));
    }

    private String formatRupiah(int price) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(price).replace("Rp", "Rp.").replace(",00", ",-");
    }

    private void showActionMenuDialog(Context context, Menu menu) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_action_menu, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Atur background dari dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);

        dialogView.findViewById(R.id.btnEdit).setOnClickListener(view -> {
            showEditMenuDialog(context, menu);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnDelete).setOnClickListener(view -> {
            deleteMenu(menu.getMenuId());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditMenuDialog(Context context, Menu menu) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_form_menu, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Atur background dari dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText inputMenuName = dialogView.findViewById(R.id.inputMenuName);
        EditText inputPrice = dialogView.findViewById(R.id.inputPrice);

        loadCategories(context, spinnerCategory, menu);  // Menambahkan `menu` sebagai parameter

        inputMenuName.setText(menu.getMenuName());
        inputPrice.setText(String.valueOf(menu.getMenuPrice()));

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(view -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(view -> {
            String menuName = inputMenuName.getText().toString();
            String categoryName = spinnerCategory.getSelectedItem().toString();
            int price = Integer.parseInt(inputPrice.getText().toString());
            updateMenu(menu.getMenuId(), menuName, categoryName, price);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadCategories(Context context, Spinner spinnerCategory, Menu menu) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, categories);
                spinnerCategory.setAdapter(adapter);

                // Set selected item to the category name of the menu being edited
                int index = categories.indexOf(menu.getCategoryName());
                if (index != -1) {
                    spinnerCategory.setSelection(index);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteMenu(String menuId) {
        FirebaseHelper.getMenusReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(menuId).removeValue();
    }

    private void updateMenu(String menuId, String menuName, String categoryName, int price) {
        FirebaseHelper.getMenusReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(menuId)
                .setValue(new Menu(menuId, menuName, categoryName, price));
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView menuNameTextView, categoryNameTextView, priceTextView;
        ImageButton btnActionMenu;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            menuNameTextView = itemView.findViewById(R.id.menuNameTextView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            btnActionMenu = itemView.findViewById(R.id.btnActionMenu);
        }
    }
}
