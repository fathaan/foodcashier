package com.foodcash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.DailyIncomeTracker;
import com.foodcash.R;
import com.foodcash.adapters.OrderDetailAdapter;
import com.foodcash.models.OrderDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDetailOrder;
    private TextView cartPrice;
    private Button btnLanjut;
    private List<OrderDetail.Item> selectedItems;
    private int totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        getSupportActionBar().hide();

        recyclerViewDetailOrder = findViewById(R.id.recyclerViewDetailOrder);
        cartPrice = findViewById(R.id.cartPrice);
        btnLanjut = findViewById(R.id.btnLanjut);

        selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
        totalAmount = getIntent().getIntExtra("totalAmount", 0);

        recyclerViewDetailOrder.setLayoutManager(new LinearLayoutManager(this));
        OrderDetailAdapter adapter = new OrderDetailAdapter(selectedItems);
        recyclerViewDetailOrder.setAdapter(adapter);

        cartPrice.setText(formatToRupiah(totalAmount));

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        btnLanjut.setOnClickListener(v -> saveOrderToFirebase());
    }

    private void saveOrderToFirebase() {
        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference the orders path inside the current user's data
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("orders");

        // Generate a unique order ID using Firebase push key
        String orderId = ordersRef.push().getKey();

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create an OrderDetail object
        OrderDetail orderDetail = new OrderDetail(orderId, currentDate, currentTime, selectedItems, totalAmount);

        if (orderId != null) {
            // Save to Firebase and pass the orderId to OrderReceiptActivity on success
            ordersRef.child(orderId).setValue(orderDetail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update daily income in MainActivity
                    DailyIncomeTracker dailyIncomeTracker = new DailyIncomeTracker(this, userId);
                    dailyIncomeTracker.addIncome(totalAmount);

                    // Go to OrderReceiptActivity and pass the orderId
                    Intent intent = new Intent(OrderDetailActivity.this, OrderReceiptActivity.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }
}
