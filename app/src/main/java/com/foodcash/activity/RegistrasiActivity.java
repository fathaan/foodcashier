package com.foodcash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class RegistrasiActivity extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword, btnBack;
    private Button btnRegister;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);
        getSupportActionBar().hide();

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnBack = findViewById(R.id.btnBack);
        btnRegister = findViewById(R.id.btnRegister);

        // Back button logic
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrasiActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, btnTogglePassword));

        // Toggle confirm password visibility
        btnToggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword));

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) ||
                    TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(RegistrasiActivity.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com")) {
                Toast.makeText(RegistrasiActivity.this, "Masukkan email Google yang valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.length() < 10) {
                Toast.makeText(RegistrasiActivity.this, "Masukkan nomor telepon yang valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegistrasiActivity.this, "Password dan konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Get user ID
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();

                        // Save user to Realtime Database
                        User user = new User(userId, name, username, email, phone);
                        usersRef.child(userId).setValue(user).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                // Kirim email verifikasi
                                firebaseUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        // Email verifikasi berhasil dikirim, pindah ke VerifikasiEmailActivity
                                        Toast.makeText(RegistrasiActivity.this,
                                                "Registrasi berhasil! Email verifikasi telah dikirim.",
                                                Toast.LENGTH_SHORT).show();

                                        // Intent ke VerifikasiEmailActivity
                                        Intent intent = new Intent(RegistrasiActivity.this, VerifikasiEmailActivity.class);
                                        intent.putExtra("email", email); // Mengirim data email ke activity
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegistrasiActivity.this,
                                                "Gagal mengirim email verifikasi.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(RegistrasiActivity.this,
                                        "Gagal menyimpan data user",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(RegistrasiActivity.this,
                            "Registrasi gagal: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Method untuk toggle visibilitas password
    private void togglePasswordVisibility(EditText editText, ImageButton toggleButton) {
        boolean isVisible = editText.getInputType() == (android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        if (isVisible) {
            // Hide password
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eyeoff);
        } else {
            // Show password
            editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
            toggleButton.setImageResource(R.drawable.ic_eyeon);
        }
        editText.setSelection(editText.getText().length()); // Place cursor at the end
    }

    public static class User {
        public String userId, name, username, email, phone;

        public User() {
        }

        public User(String userId, String name, String username, String email, String phone) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.email = email;
            this.phone = phone;
        }
    }
}
