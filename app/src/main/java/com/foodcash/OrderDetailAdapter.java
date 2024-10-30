package com.foodcash;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private List<OrderDetail.Item> orderItems;

    public OrderDetailAdapter(List<OrderDetail.Item> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail.Item item = orderItems.get(position);
        holder.menuName.setText(item.getMenuName());
        holder.menuPrice.setText(formatToRupiah(item.getMenuPrice()));
        holder.menuQuantity.setText("x" + item.getQuantity());
        holder.menuTotalPrice.setText(formatToRupiah(item.getQuantity() * item.getMenuPrice()));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
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
}
