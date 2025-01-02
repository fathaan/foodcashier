package com.foodcash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifikasiEmailActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnReload, btnLanjutkan;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_email);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        tvStatus = findViewById(R.id.tvStatus);
        btnReload = findViewById(R.id.btnReload);
        btnLanjutkan = findViewById(R.id.btnLanjutkan);

        // Periksa status verifikasi saat pertama kali dibuka
        checkEmailVerificationStatus();

        // Tombol Reload untuk memeriksa ulang status verifikasi
        btnReload.setOnClickListener(v -> checkEmailVerificationStatus());

        // Tombol Lanjutkan untuk login
        btnLanjutkan.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(VerifikasiEmailActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkEmailVerificationStatus() {
        if (currentUser != null) {
            // Refresh pengguna untuk mendapatkan status terkini
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        tvStatus.setText("Selamat! Akun Anda sudah terverifikasi.\nKlik Lanjutkan untuk login");
                        btnLanjutkan.setVisibility(Button.VISIBLE);
                        btnReload.setVisibility(Button.GONE);
                    } else {
                        tvStatus.setText("Sedang Menunggu Verifikasi Email");
                        Toast.makeText(this, "Email belum diverifikasi. Silakan cek email Anda.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Gagal memeriksa status verifikasi. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan. Silakan login kembali.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
