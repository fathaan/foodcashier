package com.foodcash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderMenuAdapter extends RecyclerView.Adapter<OrderMenuAdapter.MenuViewHolder> {

    private List<OrderMenu> menuList;
    private Context context;
    private CartListener cartListener;

    public OrderMenuAdapter(Context context, List<OrderMenu> menuList, CartListener cartListener) {
        this.context = context;
        this.menuList = menuList;
        this.cartListener = cartListener;
    }

    @Override
    public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MenuViewHolder holder, int position) {
        OrderMenu menu = menuList.get(position);
        holder.tvMenuName.setText(menu.getMenuName());
        holder.tvMenuPrice.setText(formatToRupiah(menu.getMenuPrice()));
        holder.tvQuantity.setText(String.valueOf(menu.getQuantity()));

        holder.btnIncrease.setOnClickListener(v -> {
            menu.setQuantity(menu.getQuantity() + 1);
            holder.tvQuantity.setText(String.valueOf(menu.getQuantity()));
            cartListener.onCartUpdated();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (menu.getQuantity() > 0) {
                menu.setQuantity(menu.getQuantity() - 1);
                holder.tvQuantity.setText(String.valueOf(menu.getQuantity()));
                cartListener.onCartUpdated();
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvMenuName, tvMenuPrice, tvQuantity;
        ImageButton btnIncrease, btnDecrease;

        public MenuViewHolder(View itemView) {
            super(itemView);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
            tvMenuPrice = itemView.findViewById(R.id.tvMenuPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}
