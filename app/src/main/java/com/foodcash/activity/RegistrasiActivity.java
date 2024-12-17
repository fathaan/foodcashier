package com.foodcash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class RegistrasiActivity extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

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
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) ||
                    TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegistrasiActivity.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
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
                        User user = new User(userId, name, username, email);
                        usersRef.child(userId).setValue(user).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(RegistrasiActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrasiActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegistrasiActivity.this, "Gagal menyimpan data user", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(RegistrasiActivity.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public static class User {
        public String userId, name, username, email;

        public User() {
        }

        public User(String userId, String name, String username, String email) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.email = email;
        }
    }
}
