package com.foodcash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvCurrentDateTime;
    private Handler handler = new Handler();
    private TextView tvUserName;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Button btnReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference(); // Retrieve reference from FirebaseHelper

        // Initialize TextView for displaying the user's name
        tvUserName = findViewById(R.id.tvUserName);

        // Display the user's name in TextView
        setUserName();

        // Find TextView to display date and time
        tvCurrentDateTime = findViewById(R.id.tvCurrentDateTime);

        // Start clock updates
        startClock();

        // Initialize buttons and set onClick listeners
        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Button btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        Button btnPembayaran = findViewById(R.id.btnOrder);
        btnPembayaran.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrderActivity.class);
            startActivity(intent);
        });

        Button btnCategory = findViewById(R.id.btnCategory);
        btnCategory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            startActivity(intent);
        });

        Button btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        // Initialize and set btnReport text for daily revenue
        btnReport = findViewById(R.id.btnReport);
        btnReport.setOnClickListener(v -> {
            // Open ReportIncomeActivity when btnReport is clicked
            Intent intent = new Intent(MainActivity.this, ReportIncomeActivity.class);
            startActivity(intent);
        });
    }

    private void startClock() {
        // Runnable to update time every second
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss 'WIB' | EEEE, dd MMMM yyyy", new Locale("id", "ID"));
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
    }

    private void updateDailyIncome() {
        // Get current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create DailyIncomeTracker with user ID
        DailyIncomeTracker dailyIncomeTracker = new DailyIncomeTracker(this, userId);
        int dailyIncome = dailyIncomeTracker.getDailyIncome();

        // Set text for daily income report button
        // btnReport.setText(String.format("Pemasukan Hari Ini \nRp.%s,-", formatToRupiah(dailyIncome)));
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "").replace(",00", "");
    }
}
