package com.foodcash.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.foodcash.adapters.HistoryAdapter;
import com.foodcash.models.History;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private List<Object> historyList;
    private DatabaseReference ordersRef;
    private TextView historyTitle;
    private String filterType = "ALL"; // Default filter
    private Calendar customDate = Calendar.getInstance(); // For Custom Date filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().hide();

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList);
        recyclerViewHistory.setAdapter(adapter);

        historyTitle = findViewById(R.id.historyTitle);

        // Set up Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseHelper.getOrdersReference(userId);

        // Load data from Firebase
        loadOrderHistory();

        // Back button functionality
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Filter buttons
        Button btnToday = findViewById(R.id.btnToday);
        Button btnThisWeek = findViewById(R.id.btnThisWeek);
        Button btnThisMonth = findViewById(R.id.btnThisMonth);
        Button btnCustomDate = findViewById(R.id.btnCustomDate);
        Button btnAllDay = findViewById(R.id.btnAllDay);

        btnToday.setOnClickListener(v -> {
            filterType = "TODAY";
            historyTitle.setText("Riwayat Transaksi Hari Ini");
            loadOrderHistory();
        });

        btnThisWeek.setOnClickListener(v -> {
            filterType = "WEEK";
            historyTitle.setText("Riwayat Transaksi Minggu Ini");
            loadOrderHistory();
        });

        btnThisMonth.setOnClickListener(v -> {
            filterType = "MONTH";
            historyTitle.setText("Riwayat Transaksi Bulan Ini");
            loadOrderHistory();
        });

        btnCustomDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        btnAllDay.setOnClickListener(v -> {
            filterType = "ALL";
            historyTitle.setText("Riwayat Transaksi");
            loadOrderHistory();
        });
    }

    private void loadOrderHistory() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Object> tempHistoryList = new ArrayList<>();
                List<String> tempDates = new ArrayList<>();
                Map<String, List<History>> groupedHistory = new HashMap<>();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    String orderId = orderSnapshot.child("orderId").getValue(String.class);
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    String orderTime = orderSnapshot.child("orderTime").getValue(String.class);
                    Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);

                    if (orderDate != null && shouldDisplayOrder(orderDate)) {
                        // Group data by date
                        if (!groupedHistory.containsKey(orderDate)) {
                            groupedHistory.put(orderDate, new ArrayList<>());
                            tempDates.add(orderDate);
                        }

                        History history = new History(formatToRupiah(totalPrice), orderTime, orderId);
                        groupedHistory.get(orderDate).add(history);
                    }
                }

                // Sort dates in descending order
                tempDates.sort((date1, date2) -> date2.compareTo(date1)); // Descending order

                for (String date : tempDates) {
                    // Sort history items within each date by time (descending order)
                    List<History> historyForDate = groupedHistory.get(date);
                    historyForDate.sort((h1, h2) -> h2.getTime().compareTo(h1.getTime())); // Time descending

                    tempHistoryList.add(formatDate(date)); // Add formatted date as header
                    tempHistoryList.addAll(historyForDate); // Add sorted transactions under the date
                }

                historyList.clear();
                historyList.addAll(tempHistoryList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }


    private boolean shouldDisplayOrder(String orderDate) {
        if (orderDate == null) return false;

        Calendar calendar = Calendar.getInstance();
        String[] dateParts = orderDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-indexed
        int day = Integer.parseInt(dateParts[2]);

        Calendar orderCalendar = Calendar.getInstance();
        orderCalendar.set(year, month, day);

        switch (filterType) {
            case "TODAY":
                return isSameDay(orderCalendar, calendar);
            case "WEEK":
                return isSameWeek(orderCalendar, calendar);
            case "MONTH":
                return isSameMonth(orderCalendar, calendar);
            case "CUSTOM":
                return isSameDay(orderCalendar, customDate);
            case "ALL":
                return true; // No filter
            default:
                return true;
        }
    }

    private boolean isSameDay(Calendar orderCalendar, Calendar currentCalendar) {
        return currentCalendar.get(Calendar.YEAR) == orderCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == orderCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar orderCalendar, Calendar currentCalendar) {
        currentCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return currentCalendar.get(Calendar.WEEK_OF_YEAR) == orderCalendar.get(Calendar.WEEK_OF_YEAR) &&
                currentCalendar.get(Calendar.YEAR) == orderCalendar.get(Calendar.YEAR);
    }

    private boolean isSameMonth(Calendar orderCalendar, Calendar currentCalendar) {
        return currentCalendar.get(Calendar.MONTH) == orderCalendar.get(Calendar.MONTH) &&
                currentCalendar.get(Calendar.YEAR) == orderCalendar.get(Calendar.YEAR);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerTheme, // Gunakan tema custom
                (view, year, month, dayOfMonth) -> {
                    customDate.set(year, month, dayOfMonth);
                    filterType = "CUSTOM";
                    String customDateString = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"))
                            .format(customDate.getTime());
                    historyTitle.setText("Riwayat Transaksi\n" + customDateString);
                    loadOrderHistory();
                },
                customDate.get(Calendar.YEAR),
                customDate.get(Calendar.MONTH),
                customDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }


    private String formatDate(String orderDate) {
        String[] dateParts = orderDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-indexed
        int day = Integer.parseInt(dateParts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        return sdf.format(calendar.getTime());
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }
}
