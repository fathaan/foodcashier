package com.foodcash;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmailPhone, etInputOtp;
    private Button btnSendOtp, btnNextResetPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmailPhone = findViewById(R.id.etEmailPhone);
        etInputOtp = findViewById(R.id.etInputOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnNextResetPassword = findViewById(R.id.btnNextResetPassword);

        mAuth = FirebaseAuth.getInstance();

        // Menyembunyikan input OTP dan tombol reset password awalnya
        etInputOtp.setVisibility(View.GONE);
        btnNextResetPassword.setVisibility(View.GONE);

        btnSendOtp.setOnClickListener(v -> {
            String emailOrPhone = etEmailPhone.getText().toString();

            // Mengirim OTP ke email untuk reset password
            mAuth.sendPasswordResetEmail(emailOrPhone)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email OTP terkirim", Toast.LENGTH_SHORT).show();
                            // Menampilkan input OTP dan tombol reset password setelah mengirim email
                            etInputOtp.setVisibility(View.VISIBLE);
                            btnNextResetPassword.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnNextResetPassword.setOnClickListener(v -> {
            String newPassword = etInputOtp.getText().toString();
            // Ganti password di sini
            resetPassword(newPassword);
        });
    }

    private void resetPassword(String newPassword) {
        // Di sini kamu tidak bisa memverifikasi OTP karena Firebase hanya mengirimkan link untuk reset password
        // Namun kita bisa langsung mengganti password
        if (!newPassword.isEmpty()) {
            FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                            finish(); // Menutup ForgotPasswordActivity
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Gagal mengubah password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(ForgotPasswordActivity.this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
        }
    }
}
