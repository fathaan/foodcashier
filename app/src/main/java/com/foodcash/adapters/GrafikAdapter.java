package com.foodcash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.R;
import com.foodcash.models.Grafik;

import java.util.List;

public class GrafikAdapter extends RecyclerView.Adapter<GrafikAdapter.GrafikViewHolder> {

    private List<Grafik> grafikList;

    public GrafikAdapter(List<Grafik> grafikList) {
        this.grafikList = grafikList;
    }

    @NonNull
    @Override
    public GrafikViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grafik, parent, false);
        return new GrafikViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrafikViewHolder holder, int position) {
        Grafik grafik = grafikList.get(position);
        holder.tvMenuName.setText(grafik.getMenuName());
        holder.tvQuantity.setText(String.valueOf(grafik.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return grafikList.size();
    }

    static class GrafikViewHolder extends RecyclerView.ViewHolder {
        TextView tvMenuName, tvQuantity;

        public GrafikViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}