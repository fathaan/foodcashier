package com.foodcash.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.DailyReport;
import com.foodcash.MonthlyReport;
import com.foodcash.R;
import com.foodcash.adapters.ReportIncomeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReportIncomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewReportIncome;
    private ReportIncomeAdapter reportIncomeAdapter;
    private List<Object> reportItems = new ArrayList<>(); // Menyimpan item laporan bulanan dan harian

    private FirebaseAuth mAuth;
    private DatabaseReference ordersRef;
    private TextView reportTitle;

    private String filterType = "ALL"; // Default filter
    private Calendar customDate = Calendar.getInstance(); // Untuk filter tanggal khusus

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_income);
        getSupportActionBar().hide();

        // Inisialisasi Firebase Auth dan Database Reference
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("orders");

        // Inisialisasi RecyclerView dan Adapter
        recyclerViewReportIncome = findViewById(R.id.recyclerViewReportIncome);
        recyclerViewReportIncome.setLayoutManager(new LinearLayoutManager(this));
        reportIncomeAdapter = new ReportIncomeAdapter(reportItems);
        recyclerViewReportIncome.setAdapter(reportIncomeAdapter);

        reportTitle = findViewById(R.id.reportTitle);

        // Tombol Kembali
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Tombol filter
        Button btnToday = findViewById(R.id.btnToday);
        Button btnThisWeek = findViewById(R.id.btnThisWeek);
        Button btnThisMonth = findViewById(R.id.btnThisMonth);
        Button btnCustomDate = findViewById(R.id.btnCustomDate);
        Button btnAllDay = findViewById(R.id.btnAllDay);

        btnToday.setOnClickListener(v -> {
            filterType = "TODAY";
            reportTitle.setText("Laporan Keuangan Hari Ini");
            loadOrdersData();
        });

        btnThisWeek.setOnClickListener(v -> {
            filterType = "WEEK";
            reportTitle.setText("Laporan Keuangan Minggu Ini");
            loadOrdersData();
        });

        btnThisMonth.setOnClickListener(v -> {
            filterType = "MONTH";
            reportTitle.setText("Laporan Keuangan Bulan Ini");
            loadOrdersData();
        });

        btnCustomDate.setOnClickListener(v -> showDatePickerDialog());

        btnAllDay.setOnClickListener(v -> {
            filterType = "ALL";
            reportTitle.setText("Laporan Keuangan");
            loadOrdersData();
        });

        // Muat data awal
        loadOrdersData();
    }

    private void loadOrdersData() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportItems.clear();

                Map<String, Integer> monthlyTotals = new HashMap<>();
                TreeMap<String, DailyReport> dailyReports = new TreeMap<>(Comparator.reverseOrder());

                Calendar currentCalendar = Calendar.getInstance();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);

                    if (orderDate != null && totalPrice != null) {
                        Calendar orderCalendar = Calendar.getInstance();
                        String[] dateParts = orderDate.split("-");
                        int year = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]) - 1;
                        int day = Integer.parseInt(dateParts[2]);
                        orderCalendar.set(year, month, day);

                        if (shouldDisplayOrder(orderCalendar)) {
                            String monthKey = orderDate.substring(0, 7); // yyyy-MM

                            // Update total bulanan
                            monthlyTotals.put(monthKey, monthlyTotals.getOrDefault(monthKey, 0) + totalPrice);

                            // Update laporan harian
                            DailyReport dailyReport = dailyReports.getOrDefault(orderDate, new DailyReport(orderDate, 0, 0));
                            dailyReport.incrementTotalPrice(totalPrice);
                            dailyReport.incrementTotalCustomers();
                            dailyReports.put(orderDate, dailyReport);
                        }
                    }
                }

                // Tambahkan laporan bulanan dan harian ke daftar item
                for (String month : monthlyTotals.keySet()) {
                    reportItems.add(new MonthlyReport(month, monthlyTotals.get(month)));

                    for (String dailyKey : dailyReports.keySet()) {
                        if (dailyKey.startsWith(month)) {
                            reportItems.add(dailyReports.get(dailyKey));
                        }
                    }
                }

                reportIncomeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportIncomeActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean shouldDisplayOrder(Calendar orderCalendar) {
        Calendar currentCalendar = Calendar.getInstance();

        switch (filterType) {
            case "TODAY":
                return isSameDay(orderCalendar, currentCalendar);
            case "WEEK":
                return isSameWeek(orderCalendar, currentCalendar);
            case "MONTH":
                return isSameMonth(orderCalendar, currentCalendar);
            case "CUSTOM":
                return isSameDay(orderCalendar, customDate);
            case "ALL":
            default:
                return true;
        }
    }

    private boolean isSameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar calendar1, Calendar calendar2) {
        calendar2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR) &&
                calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    private boolean isSameMonth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerTheme, // Pastikan menggunakan tema khusus
                (view, year, month, dayOfMonth) -> {
                    customDate.set(year, month, dayOfMonth);
                    filterType = "CUSTOM";
                    String customDateString = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"))
                            .format(customDate.getTime());
                    reportTitle.setText("Laporan Keuangan\n" + customDateString);
                    loadOrdersData();
                },
                customDate.get(Calendar.YEAR),
                customDate.get(Calendar.MONTH),
                customDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
}
