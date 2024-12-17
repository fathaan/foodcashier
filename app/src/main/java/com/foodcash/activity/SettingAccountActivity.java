package com.foodcash.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.foodcash.FirebaseHelper;
import com.foodcash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Button btnEditName, btnEditUsername, btnEditEmail, btnEditPassword;

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
        btnEditPassword = findViewById(R.id.btnEditPassword);

        // Ambil data user dari Firebase dan tampilkan di tombol
        loadUserData();

        // Listener untuk edit nama
        btnEditName.setOnClickListener(v -> showEditDialog("name", "Edit Nama", btnEditName.getText().toString().replace("Edit Nama: ", "")));

        // Listener untuk edit username
        btnEditUsername.setOnClickListener(v -> showEditDialog("username", "Edit Username", btnEditUsername.getText().toString().replace("Edit Username: ", "")));

        // Listener untuk edit email
        btnEditEmail.setOnClickListener(v -> showEditDialog("email", "Edit Email", btnEditEmail.getText().toString().replace("Edit Email: ", "")));

        // Listener untuk edit password
        btnEditPassword.setOnClickListener(v -> showEditPasswordDialog());
    }

    // Method untuk mengambil data user dari Firebase
    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Tampilkan data pada tombol
                    btnEditName.setText("Edit Nama: " + name);
                    btnEditUsername.setText("Edit Username: " + username);
                    btnEditEmail.setText("Edit Email: " + email);
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

    // Method untuk menampilkan dialog edit nama, username, email dengan nilai awal
    private void showEditDialog(String field, String title, String currentValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate layout custom
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(customLayout);

        // Set judul dialog
        TextView dialogTitle = customLayout.findViewById(R.id.dialogTitle);
        dialogTitle.setText(title);

        // Mengambil EditText dari layout custom dan set nilai awal
        final EditText inputField = customLayout.findViewById(R.id.inputField);
        inputField.setText(currentValue);  // Isi dengan nilai saat ini

        // Mengambil tombol dari layout custom
        Button btnSimpan = customLayout.findViewById(R.id.btnSimpan);
        Button btnBatal = customLayout.findViewById(R.id.btnBatal);

        // Buat dialog dari builder
        AlertDialog dialog = builder.create();

        // Set background drawable secara programatis
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        // Listener untuk tombol Simpan
        btnSimpan.setOnClickListener(v -> {
            String newValue = inputField.getText().toString();
            if (!newValue.isEmpty()) {
                updateUserInfo(field, newValue);  // Memperbarui data di Firebase
                dialog.dismiss();  // Tutup dialog setelah simpan
            } else {
                Toast.makeText(this, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener untuk tombol Batal
        btnBatal.setOnClickListener(v -> dialog.dismiss());

        // Menampilkan dialog
        dialog.show();
    }



    // Method untuk update info user di Firebase Database
    private void updateUserInfo(String field, String value) {
        String userId = mAuth.getCurrentUser().getUid();
        usersRef.child(userId).child(field).setValue(value)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, field + " berhasil diperbarui", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui " + field, Toast.LENGTH_SHORT).show());
    }

    // Method khusus untuk edit password
    private void showEditPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate layout custom untuk password
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_edit_profile_password, null);
        builder.setView(customLayout);

        // Mengambil EditText dari layout custom
        final EditText inputOldPassword = customLayout.findViewById(R.id.inputOldPassword);
        final EditText inputNewPassword = customLayout.findViewById(R.id.inputNewPassword);
        final EditText inputConfirmPassword = customLayout.findViewById(R.id.inputConfirmPassword);

        // Membuat dialog dari builder
        AlertDialog dialog = builder.create();

        // Set background drawable secara programatis
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }

        // Listener untuk tombol Simpan
        customLayout.findViewById(R.id.btnSimpan).setOnClickListener(v -> {
            String oldPassword = inputOldPassword.getText().toString();
            String newPassword = inputNewPassword.getText().toString();
            String confirmPassword = inputConfirmPassword.getText().toString();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifikasi password lama
            // Jika password lama benar, lanjutkan dengan mengganti password
            mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), oldPassword)
                    .addOnSuccessListener(authResult -> {
                        // Cek apakah password baru dan konfirmasi password sama
                        if (newPassword.equals(confirmPassword)) {
                            mAuth.getCurrentUser().updatePassword(newPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui password", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Password lama salah", Toast.LENGTH_SHORT).show();
                    });

        });

        // Listener untuk tombol Batal
        customLayout.findViewById(R.id.btnBatal).setOnClickListener(v -> dialog.dismiss());

        // Menampilkan dialog
        dialog.show();
    }
}
