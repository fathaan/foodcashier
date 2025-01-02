package com.foodcash.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsernameOrEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private boolean isPasswordVisible = false; // Default password visibility state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        // Firebase Auth dan referensi ke database
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();

        // Inisialisasi UI
        etUsernameOrEmail = findViewById(R.id.etUsernameOrEmail);
        etPassword = findViewById(R.id.etPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword); // Tombol toggle password
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Default password disembunyikan, tombol menggunakan ic_eyeon
        btnTogglePassword.setImageResource(R.drawable.ic_eyeon);

        // Tambahkan logika untuk toggle visibilitas password
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Ketika login button diklik
        btnLogin.setOnClickListener(v -> {
            String input = etUsernameOrEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(input) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Username/Email dan Password harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Jika input adalah kombinasi khusus, arahkan ke RegistrasiActivity
            if (input.equals("regiskasir") && password.equals("adminfagl")) {
                Toast.makeText(LoginActivity.this, "Login sebagai admin berhasil", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, RegistrasiActivity.class));
                finish();
                return;
            }

            // Cek apakah input adalah email
            if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                // Jika email, langsung login
                loginWithEmail(input, password);
            } else {
                // Jika bukan email, anggap sebagai username
                loginWithUsername(input, password);
            }
        });

        // Ketika teks "Lupa Password" diklik
        tvForgotPassword.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(forgotPasswordIntent);
        });
    }

    // Method untuk toggle visibilitas password
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Jika password sedang terlihat, sembunyikan
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eyeon); // Ganti ikon menjadi ic_eyeon
        } else {
            // Jika password tersembunyi, tampilkan
            etPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
            btnTogglePassword.setImageResource(R.drawable.ic_eyeoff); // Ganti ikon menjadi ic_eyeoff
        }
        isPasswordVisible = !isPasswordVisible; // Toggle state
        etPassword.setSelection(etPassword.getText().length()); // Letakkan kursor di akhir teks
    }

    private void loginWithUsername(String username, String password) {
        // Cari email berdasarkan username
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null) {
                            loginWithEmail(email, password); // Panggil loginWithEmail untuk verifikasi lebih lanjut
                        } else {
                            Toast.makeText(LoginActivity.this, "Email untuk username ini tidak belum terverifikasi.", Toast.LENGTH_SHORT).show();
                        }
                        break; // Hanya satu akun yang relevan
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // Simpan status login ke SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true); // Status login tersimpan
                    editor.apply();

                    // Arahkan ke MainActivity
                    Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Jika email belum diverifikasi
                    Toast.makeText(LoginActivity.this, "Akun belum terverifikasi. Silakan cek email Anda.", Toast.LENGTH_LONG).show();
                    // Optional: Kirim ulang email verifikasi
                    if (user != null) {
                        user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                            if (verificationTask.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Email verifikasi telah dikirim ulang.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Gagal mengirim email verifikasi: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                // Jika login gagal
                Toast.makeText(LoginActivity.this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
