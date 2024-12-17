package com.foodcash.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.CartListener;
import com.foodcash.models.OrderCategory;
import com.foodcash.R;

import java.util.List;

public class OrderCategoryAdapter extends RecyclerView.Adapter<OrderCategoryAdapter.CategoryViewHolder> {

    private List<OrderCategory> categoryList;
    private Context context;
    private CartListener cartListener;

    public OrderCategoryAdapter(Context context, List<OrderCategory> categoryList, CartListener cartListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.cartListener = cartListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_list, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        OrderCategory category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getCategoryName());

        // Initialize and set LayoutManager and adapter for menu items within the category
        OrderMenuAdapter menuAdapter = new OrderMenuAdapter(context, category.getMenuList(), cartListener);
        holder.rvMenuItems.setLayoutManager(new LinearLayoutManager(context));
        holder.rvMenuItems.setAdapter(menuAdapter);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        RecyclerView rvMenuItems;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            rvMenuItems = itemView.findViewById(R.id.rvMenuItems);
        }
    }
}
