package com.foodcash.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.foodcash.FirebaseHelper;
import com.foodcash.MyValueFormatter;
import com.foodcash.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GrafikActivity extends AppCompatActivity {

    private PieChart pieChart;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Button btnToday, btnThisWeek, btnThisMonth, btnCustomDate;
    private TextView tvMenu, tvNoDataMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);
        getSupportActionBar().hide();

        pieChart = findViewById(R.id.pieChart);
        tvMenu = findViewById(R.id.tvMenu);  // Initialize TextView
        tvNoDataMessage = findViewById(R.id.tvNoDataMessage); // Initialize the No Data TextView

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());

        btnToday = findViewById(R.id.btnToday);
        btnThisWeek = findViewById(R.id.btnThisWeek);
        btnThisMonth = findViewById(R.id.btnThisMonth);
        btnCustomDate = findViewById(R.id.btnCustomDate);

        btnToday.setOnClickListener(v -> loadChartData("today"));
        btnThisWeek.setOnClickListener(v -> loadChartData("week"));
        btnThisMonth.setOnClickListener(v -> loadChartData("month"));
        btnCustomDate.setOnClickListener(v -> showDatePicker());

        // Load default data for today
        loadChartData("today");
    }

    private void loadChartData(String filter) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference ordersRef = usersRef.child(userId).child("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> menuCountMap = new HashMap<>();
                boolean hasDataForDate = false;

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    if (isDateInRange(orderDate, filter)) {
                        hasDataForDate = true;
                        for (DataSnapshot item : orderSnapshot.child("orderedItems").getChildren()) {
                            String menuName = item.child("menuName").getValue(String.class);
                            int quantity = item.child("quantity").getValue(Integer.class);

                            menuCountMap.put(menuName, menuCountMap.getOrDefault(menuName, 0) + quantity);
                        }
                    }
                }

                if (hasDataForDate) {
                    displayMenuTerlaris(menuCountMap); // Display Menu Terlaris
                    displayPieChart(menuCountMap); // Display Pie Chart
                    tvNoDataMessage.setVisibility(View.GONE); // Hide "No Data" message
                } else {
                    pieChart.clear();
                    displayNoDataMessage(filter); // Show No Data Message
                }

                // Update the text of tvMenu based on the selected filter
                updateMenuTitle(filter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GrafikActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isDateInRange(String orderDate, String filter) {
        Calendar calendar = Calendar.getInstance();
        Calendar orderCalendar = Calendar.getInstance();

        // Mengonversi string tanggal ke Calendar
        String[] dateParts = orderDate.split(" ")[0].split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Bulan dimulai dari 0
        int day = Integer.parseInt(dateParts[2]);
        orderCalendar.set(year, month, day);

        switch (filter) {
            case "today":
                return orderCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        orderCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
            case "week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                long startOfWeek = calendar.getTimeInMillis();
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                long endOfWeek = calendar.getTimeInMillis();
                return orderCalendar.getTimeInMillis() >= startOfWeek && orderCalendar.getTimeInMillis() <= endOfWeek;
            case "month":
                return orderCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        orderCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);
            default:
                return false;
        }
    }

    private void displayPieChart(Map<String, Integer> menuCountMap) {
        if (menuCountMap.isEmpty()) {
            Toast.makeText(GrafikActivity.this, "No data available for Pie Chart.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : menuCountMap.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            colors.add(getRandomColor());
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setValueTextColor(Color.BLUE);

        // Gunakan ValueFormatter untuk menghilangkan desimal
        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Format nilai sebagai bilangan bulat
            }
        });

        PieData pieData = new PieData(pieDataSet);

        // Untuk menampilkan legend secara vertikal
        Legend legend = pieChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL); // Vertikal
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER); // Tengah
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT); // Menempel di kanan

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.BLUE);
        pieChart.setDrawEntryLabels(false);

        pieChart.invalidate(); // Refresh chart
    }

    private void displayMenuTerlaris(Map<String, Integer> menuCountMap) {
        if (menuCountMap.isEmpty()) {
            Toast.makeText(GrafikActivity.this, "No menu data available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Urutkan berdasarkan jumlah yang paling tinggi
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(menuCountMap.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Urutkan berdasarkan count, terbesar di atas

        LinearLayout tableMenuTerlaris = findViewById(R.id.tableMenuTerlaris);
        tableMenuTerlaris.removeAllViews(); // Hapus semua tampilan sebelumnya

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            View itemView = getLayoutInflater().inflate(R.layout.item_grafik, tableMenuTerlaris, false);

            TextView menuName = itemView.findViewById(R.id.menuName);
            TextView menuCount = itemView.findViewById(R.id.menuCount);

            menuName.setText(entry.getKey()); // Set menu name
            menuCount.setText(String.valueOf(entry.getValue())); // Set count

            tableMenuTerlaris.addView(itemView); // Tambahkan item ke table
        }
    }

    private int getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private void updateMenuTitle(String filter) {
        // Update the tvMenu TextView based on the selected filter
        switch (filter) {
            case "today":
                tvMenu.setText("Grafik Penjualan Hari ini");
                break;
            case "week":
                tvMenu.setText("Grafik Penjualan Minggu ini");
                break;
            case "month":
                tvMenu.setText("Grafik Penjualan Bulan ini");
                break;
            default:
                tvMenu.setText("Grafik Penjualan");
                break;
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerTheme,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    loadChartDataForCustomDate(selectedDate);
                }, year, month, day
        );
        datePickerDialog.show();
    }

    private void loadChartDataForCustomDate(String selectedDate) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference ordersRef = usersRef.child(userId).child("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> menuCountMap = new HashMap<>();
                boolean hasDataForDate = false;

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    if (orderDate.equals(selectedDate)) {
                        hasDataForDate = true;
                        for (DataSnapshot item : orderSnapshot.child("orderedItems").getChildren()) {
                            String menuName = item.child("menuName").getValue(String.class);
                            int quantity = item.child("quantity").getValue(Integer.class);

                            menuCountMap.put(menuName, menuCountMap.getOrDefault(menuName, 0) + quantity);
                        }
                    }
                }

                if (hasDataForDate) {
                    displayMenuTerlaris(menuCountMap); // Display Menu Terlaris
                    displayPieChart(menuCountMap); // Display Pie Chart
                    tvNoDataMessage.setVisibility(View.GONE); // Hide "No Data" message
                } else {
                    pieChart.clear();
                    displayNoDataMessage("custom"); // Show No Data Message
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GrafikActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNoDataMessage(String filter) {
        tvNoDataMessage.setVisibility(View.VISIBLE);
        String message = "Tidak ada data pada periode ini.";
        tvNoDataMessage.setText(message);

        // Clear the PieChart and the list of menu items
        pieChart.clear(); // Clear the PieChart data
        LinearLayout tableMenuTerlaris = findViewById(R.id.tableMenuTerlaris);
        tableMenuTerlaris.removeAllViews(); // Clear the list of menu items
    }

}
