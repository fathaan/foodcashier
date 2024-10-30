package com.foodcash;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private List<Object> historyList;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().hide();

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList);
        recyclerViewHistory.setAdapter(adapter);

        // Set up Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseHelper.getOrdersReference(userId);

        // Load data from Firebase
        loadOrderHistory();

        // Back button functionality
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadOrderHistory() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyList.clear();
                String previousDate = "";

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve order details
                    String orderId = orderSnapshot.child("orderId").getValue(String.class);
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    String orderTime = orderSnapshot.child("orderTime").getValue(String.class);
                    Integer totalPrice = orderSnapshot.child("totalPrice").getValue(Integer.class);

                    if (orderDate != null && !orderDate.equals(previousDate)) {
                        // Add new date header if it's a different date
                        historyList.add(orderDate);
                        previousDate = orderDate;
                    }

                    // Create History object and add it to the list
                    History history = new History(formatToRupiah(totalPrice), orderTime, orderId);
                    historyList.add(history);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }
}
