package com.foodcash.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.DailyIncomeTracker;
import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvCurrentDateTime, tvUserName, tvTotalTransaction, tvTotalProduction;
    private Button btnReport;
    private Handler handler = new Handler();
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sembunyikan ActionBar
        getSupportActionBar().hide();

        // Initialize FirebaseAuth dan DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();

        // Hubungkan elemen UI ke variabel
        tvUserName = findViewById(R.id.tvUserName);
        tvCurrentDateTime = findViewById(R.id.tvCurrentDateTime);
        tvTotalTransaction = findViewById(R.id.tvTotalTransaction);
        tvTotalProduction = findViewById(R.id.tvTotalProduction);
        btnReport = findViewById(R.id.btnReport);

        // Tombol navigasi
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnOrder = findViewById(R.id.btnOrder);
        Button btnCategory = findViewById(R.id.btnCategory);
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnGrafik = findViewById(R.id.btnGrafik);  // Tombol Grafik

        // Logika tombol navigasi
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        btnOrder.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OrderActivity.class)));
        btnCategory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryActivity.class)));
        btnMenu.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MenuActivity.class)));
        btnReport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportIncomeActivity.class)));
        btnGrafik.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GrafikActivity.class)));

        // Tampilkan nama pengguna
        setUserName();

        // Jalankan fungsi clock
        startClock();
    }


    private void startClock() {
        // Runnable to update time every second
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss 'WIB' \nEEEE, dd MMMM yyyy", new Locale("id", "ID"));
                String currentDateAndTime = sdf.format(new Date());

                // Set the result to TextView
                tvCurrentDateTime.setText(currentDateAndTime);

                // Run this update every 1000ms (1 second)
                handler.postDelayed(this, 1000);
            }
        };

        // Run the runnable for the first time
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the handler when the Activity is destroyed
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check login status from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // If not logged in, redirect to LoginActivity
        if (!isLoggedIn) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    private void setUserName() {
        // Get the user ID from FirebaseAuth
        String userId = mAuth.getCurrentUser().getUid();

        // Retrieve user data from Firebase Realtime Database
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the name from the database
                    String name = dataSnapshot.child("name").getValue(String.class);

                    // Set the name to TextView
                    tvUserName.setText(name);
                } else {
                    tvUserName.setText("Nama tidak ditemukan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDailyIncome();
        updateTransactionData();
    }

    private void updateDailyIncome() {
        // Get current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create DailyIncomeTracker with user ID
        DailyIncomeTracker dailyIncomeTracker = new DailyIncomeTracker(this, userId);
        int dailyIncome = dailyIncomeTracker.getDailyIncome();

    }

    private void updateTransactionData() {
        // Dapatkan user ID dari FirebaseAuth
        String userId = mAuth.getCurrentUser().getUid();

        // Referensi ke tabel orders
        DatabaseReference ordersRef = usersRef.child(userId).child("orders");

        // Ambil tanggal hari ini
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Listener untuk mengambil data dari Firebase
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalTransaction = 0; // Total harga transaksi
                int totalProduction = 0; // Jumlah transaksi

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);

                    // Periksa apakah orderDate sama dengan todayDate
                    if (todayDate.equals(orderDate)) {
                        Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);

                        // Hitung total transaksi dan jumlah produksi
                        if (totalPrice != null) {
                            totalTransaction += totalPrice;
                            totalProduction++;
                        }
                    }
                }

                // Tampilkan hasil di UI
                tvTotalTransaction.setText(String.format("Rp%s,-", formatToRupiah(totalTransaction)));
                tvTotalProduction.setText(String.format("%d Transaksi", totalProduction));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "").replace(",00", "");
    }
}
