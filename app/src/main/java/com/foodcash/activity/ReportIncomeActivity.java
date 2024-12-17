package com.foodcash.activity;

import android.os.Bundle;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportIncomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewReportIncome;
    private ReportIncomeAdapter reportIncomeAdapter;
    private List<Object> reportItems = new ArrayList<>(); // Menyimpan item laporan bulanan dan harian

    private FirebaseAuth mAuth;
    private DatabaseReference ordersRef;

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

        // Tombol Kembali
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Muat data pesanan dari Firebase
        loadOrdersData();
    }

    private void loadOrdersData() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> monthlyTotals = new HashMap<>();
                Map<String, DailyReport> dailyReports = new HashMap<>();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);

                    if (orderDate != null && totalPrice != null) {
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

                // Set data untuk RecyclerView
                for (String month : monthlyTotals.keySet()) {
                    reportItems.add(new MonthlyReport(month, monthlyTotals.get(month)));

                    for (DailyReport dailyReport : dailyReports.values()) {
                        if (dailyReport.getDate().startsWith(month)) {
                            reportItems.add(dailyReport);
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
}
