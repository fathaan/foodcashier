package com.foodcash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderReceiptAdapter extends RecyclerView.Adapter<OrderReceiptAdapter.ViewHolder> {

    private final List<OrderDetail.Item> orderedItems;
    private final Context context;

    public OrderReceiptAdapter(List<OrderDetail.Item> orderedItems, Context context) {
        this.orderedItems = orderedItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_receipt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail.Item item = orderedItems.get(position);

        // Set item details
        holder.menuName.setText(item.getMenuName());
        holder.menuPrice.setText(formatToRupiah(item.getMenuPrice()));
        holder.menuQuantity.setText("x" + item.getQuantity());
        holder.menuTotalPrice.setText(formatToRupiah(item.getMenuPrice() * item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return orderedItems.size();
    }

    public Iterable<? extends OrderDetail.Item> getOrderItems() {
        return orderedItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView menuName, menuPrice, menuQuantity, menuTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            menuName = itemView.findViewById(R.id.menuName);
            menuPrice = itemView.findViewById(R.id.menuPrice);
            menuQuantity = itemView.findViewById(R.id.menuQuantity);
            menuTotalPrice = itemView.findViewById(R.id.menuTotalPrice);
        }
    }

    // Helper method to format integers to Indonesian Rupiah format
    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }
}
