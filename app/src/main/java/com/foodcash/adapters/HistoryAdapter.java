package com.foodcash.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.activity.OrderReceiptActivity;
import com.foodcash.R;
import com.foodcash.models.History;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE = 0;
    private static final int TYPE_ORDER = 1;
    private List<Object> historyList;

    public HistoryAdapter(List<Object> historyList) {
        this.historyList = historyList;
    }

    @Override
    public int getItemViewType(int position) {
        return historyList.get(position) instanceof String ? TYPE_DATE : TYPE_ORDER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_order_date, parent, false);
            return new DateViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new OrderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_DATE) {
            String date = (String) historyList.get(position);
            ((DateViewHolder) holder).dateText.setText(date);
        } else {
            History history = (History) historyList.get(position);
            OrderViewHolder orderViewHolder = (OrderViewHolder) holder;
            orderViewHolder.amountText.setText(history.getAmount());
            orderViewHolder.timeText.setText(history.getTime());
            orderViewHolder.transactionIdText.setText(history.getTransactionId());

            // Set onClickListener for btnDetailOrder
            orderViewHolder.detailButton.setOnClickListener(v -> {
                // Start OrderReceiptActivity with the order ID
                Intent intent = new Intent(v.getContext(), OrderReceiptActivity.class);
                intent.putExtra("orderId", history.getTransactionId());
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView amountText, timeText, transactionIdText;
        Button detailButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.tvPrice);
            timeText = itemView.findViewById(R.id.tvTime);
            transactionIdText = itemView.findViewById(R.id.tvOrderId);
            detailButton = itemView.findViewById(R.id.btnDetailOrder);
        }
    }
}
