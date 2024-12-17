package com.foodcash.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.foodcash.FirebaseHelper;
import com.foodcash.activity.LoginActivity;
import com.foodcash.R;
import com.foodcash.activity.SettingAccountActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private TextView profileEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().hide();

        // Inisialisasi view dari layout
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnSettingAccount = findViewById(R.id.btnSettingAccount);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Inisialisasi FirebaseAuth dan DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference(); // Mengambil referensi users dari FirebaseHelper

        // Set listener untuk tombol back
        btnBack.setOnClickListener(v -> finish());

        // Set konten detail profil
        setProfileDetails();

        // Set background untuk card view atau layout lainnya
        CardView profileCard = findViewById(R.id.profileCard);
        profileCard.setBackgroundResource(R.drawable.rounded_button_sea);

        btnSettingAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingAccountActivity.class);
            startActivity(intent);
        });

        // Hapus status login saat logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Logout dari Firebase

            // Hapus status login dari SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Hapus semua data di SharedPreferences
            editor.apply();

            // Arahkan ke LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }

    // Method untuk mengisi konten profil
    private void setProfileDetails() {
        // Mendapatkan user ID dari FirebaseAuth
        String userId = mAuth.getCurrentUser().getUid();

        // Ambil data user dari Firebase Realtime Database
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ambil data nama dan email dari database
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Set nama dan email ke TextView
                    profileName.setText(name);
                    profileEmail.setText(email);
                } else {
                    // Jika data user tidak ditemukan
                    profileName.setText("Nama tidak ditemukan");
                    profileEmail.setText("Email tidak ditemukan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Menangani error saat mengambil data
                Toast.makeText(ProfileActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
