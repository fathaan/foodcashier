package com.foodcash.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Button btnEditName, btnEditUsername, btnEditEmail, btnEditNohp, btnEditPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_account);
        getSupportActionBar().hide();

        // Inisialisasi FirebaseAuth dan referensi database user
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();

        // Inisialisasi button
        btnEditName = findViewById(R.id.btnEditName);
        btnEditUsername = findViewById(R.id.btnEditUsername);
        btnEditEmail = findViewById(R.id.btnEditEmail);
        btnEditNohp = findViewById(R.id.btnEditNohp);
        btnEditPassword = findViewById(R.id.btnEditPassword);

        // Ambil data user dari Firebase dan tampilkan di tombol
        loadUserData();

        // Listener untuk edit nama
        btnEditName.setOnClickListener(v -> showEditDialog("name", "Edit Nama", btnEditName.getText().toString().split("\n")[1]));

        // Listener untuk edit username
        btnEditUsername.setOnClickListener(v -> showEditDialog("username", "Edit Username", btnEditUsername.getText().toString().split("\n")[1]));

        // Listener untuk edit email
        btnEditEmail.setOnClickListener(v -> showEditEmailDialog());

        // Listener untuk edit nomor telepon
        btnEditNohp.setOnClickListener(v -> showEditDialog("phone", "Edit Nomor Telepon", btnEditNohp.getText().toString().split("\n")[1]));

        // Listener untuk edit password
        btnEditPassword.setOnClickListener(v -> showEditPasswordDialog());
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class); // Ambil data dari key "phone"

                    // Tampilkan data pada tombol
                    btnEditName.setText("Nama Toko :\n" + (name != null ? name : ""));
                    btnEditUsername.setText("Username :\n" + (username != null ? username : ""));
                    btnEditEmail.setText("Email :\n" + (email != null ? email : ""));
                    btnEditNohp.setText("Nomor Telepon :\n" + (phone != null ? phone : ""));
                    btnEditPassword.setText("Password :\n******");
                } else {
                    Toast.makeText(SettingAccountActivity.this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingAccountActivity.this, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(String field, String title, String currentValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(customLayout);

        TextView dialogTitle = customLayout.findViewById(R.id.dialogTitle);
        dialogTitle.setText(title);

        final EditText inputField = customLayout.findViewById(R.id.inputField);
        inputField.setText(currentValue);

        Button btnSimpan = customLayout.findViewById(R.id.btnSimpan);
        Button btnBatal = customLayout.findViewById(R.id.btnBatal);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        btnSimpan.setOnClickListener(v -> {
            String newValue = inputField.getText().toString();
            if (!newValue.isEmpty()) {
                updateUserInfo(field, newValue);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateUserInfo(String field, String value) {
        String userId = mAuth.getCurrentUser().getUid();
        usersRef.child(userId).child(field).setValue(value)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, field + " berhasil diperbarui", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui " + field, Toast.LENGTH_SHORT).show());
    }

    private void showEditEmailDialog() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(customLayout);

        TextView dialogTitle = customLayout.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Edit Email");

        final EditText inputField = customLayout.findViewById(R.id.inputField);
        inputField.setText(user.getEmail());

        Button btnSimpan = customLayout.findViewById(R.id.btnSimpan);
        Button btnBatal = customLayout.findViewById(R.id.btnBatal);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        btnSimpan.setOnClickListener(v -> {
            String newEmail = inputField.getText().toString();
            if (!newEmail.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                user.updateEmail(newEmail)
                        .addOnSuccessListener(aVoid -> {
                            // Send email verification
                            user.sendEmailVerification()
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Update email in Firebase Realtime Database
                                        usersRef.child(user.getUid()).child("email").setValue(newEmail)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Toast.makeText(this, "Email berhasil diperbarui. Verifikasi email telah dikirim.", Toast.LENGTH_SHORT).show();
                                                    // Navigate to VerifikasiEmailActivity
                                                    Intent intent = new Intent(this, VerifikasiEmailActivity.class);
                                                    intent.putExtra("email", newEmail);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui email di database: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal mengirim email verifikasi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Masukkan email yang valid.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void showEditPasswordDialog() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_profile_password, null);
        builder.setView(customLayout);

        TextView dialogTitle = customLayout.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Edit Password");

        final EditText inputOldPassword = customLayout.findViewById(R.id.inputOldPassword);
        final EditText inputNewPassword = customLayout.findViewById(R.id.inputNewPassword);
        final EditText inputConfirmPassword = customLayout.findViewById(R.id.inputConfirmPassword);

        Button btnSimpan = customLayout.findViewById(R.id.btnSimpan);
        Button btnBatal = customLayout.findViewById(R.id.btnBatal);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        btnSimpan.setOnClickListener(v -> {
            String oldPassword = inputOldPassword.getText().toString();
            String newPassword = inputNewPassword.getText().toString();
            String confirmPassword = inputConfirmPassword.getText().toString();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPassword))
                    .addOnSuccessListener(aVoid -> user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui password: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> Toast.makeText(this, "Re-autentikasi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
