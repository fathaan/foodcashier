package com.foodcash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.DailyReport;
import com.foodcash.MonthlyReport;
import com.foodcash.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportIncomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MONTH = 0;
    private static final int VIEW_TYPE_DAY = 1;

    private List<Object> reportItems;

    public ReportIncomeAdapter(List<Object> reportItems) {
        this.reportItems = reportItems;
    }

    @Override
    public int getItemViewType(int position) {
        return (reportItems.get(position) instanceof MonthlyReport) ? VIEW_TYPE_MONTH : VIEW_TYPE_DAY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MONTH) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_income_date, parent, false);
            return new MonthViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_income, parent, false);
            return new DayViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_MONTH) {
            ((MonthViewHolder) holder).bind((MonthlyReport) reportItems.get(position));
        } else {
            ((DayViewHolder) holder).bind((DailyReport) reportItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {
        private TextView monthText, totalText;

        public MonthViewHolder(View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.monthText);
            totalText = itemView.findViewById(R.id.totalText);
        }

        public void bind(MonthlyReport report) {
            monthText.setText(formatMonth(report.getMonth()));
            totalText.setText(formatToRupiah(report.getTotal()));
        }

        private String formatMonth(String month) {
            try {
                // Parsing format yyyy-MM
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Date date = inputFormat.parse(month);

                // Menyusun format "MMMM yyyy" (e.g., "Bulan")
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return month;
            }
        }
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvAmountPrice, tvAmountCustomer;

        public DayViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmountPrice = itemView.findViewById(R.id.tvAmountPrice);
            tvAmountCustomer = itemView.findViewById(R.id.tvAmountCustomer);
        }

        public void bind(DailyReport report) {
            tvDate.setText(formatDate(report.getDate()));
            tvAmountPrice.setText(formatToRupiah(report.getTotal()));
            tvAmountCustomer.setText("Total " +report.getTotalCustomers() + " Pelanggan");
        }

        private String formatDate(String date) {
            try {
                // Parsing format yyyy-MM-dd
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date parsedDate = inputFormat.parse(date);

                // Format ke "EEEE, dd MMMM yyyy" (e.g., "Rabu, 30 Oktober 2024")
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return date;
            }
        }
    }

    private static String formatToRupiah(int amount) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID"))
                .format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }
}
