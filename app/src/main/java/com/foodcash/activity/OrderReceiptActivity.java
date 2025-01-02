package com.foodcash.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodcash.R;
import com.foodcash.adapters.OrderReceiptAdapter;
import com.foodcash.models.OrderDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderReceiptActivity extends AppCompatActivity {

    private TextView tvRefNumber, tvPaymentTime, tvAmount;
    private RecyclerView recyclerViewReceiptDetails;
    private Button btnGetStrukReceipt, btnGetPDFReceipt, btnBackToHome;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_receipt);
        getSupportActionBar().hide();

        // Initialize views
        tvRefNumber = findViewById(R.id.tvRefNumber);
        tvPaymentTime = findViewById(R.id.tvPaymentTime);
        tvAmount = findViewById(R.id.tvAmount);
        recyclerViewReceiptDetails = findViewById(R.id.recyclerViewReceiptDetails);
        btnGetStrukReceipt = findViewById(R.id.btnGetStrukReceipt);
        btnGetPDFReceipt = findViewById(R.id.btnGetPDFReceipt);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        recyclerViewReceiptDetails.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve order data from Firebase
        orderId = getIntent().getStringExtra("orderId");
        fetchOrderData(orderId);

        // Button logic
        btnGetPDFReceipt.setOnClickListener(v -> generatePDFReceipt());
        btnGetStrukReceipt.setOnClickListener(v -> {
            // Empty for now
        });
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderReceiptActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchOrderData(String orderId) {
        // Get the user ID of the logged-in user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("orders").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String orderId = snapshot.child("orderId").getValue(String.class);
                    String orderDate = snapshot.child("orderDate").getValue(String.class);
                    String orderTime = snapshot.child("orderTime").getValue(String.class);
                    int totalPrice = snapshot.child("totalPrice").getValue(Integer.class);

                    List<OrderDetail.Item> orderedItems = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.child("orderedItems").getChildren()) {
                        String menuId = itemSnapshot.child("menuId").getValue(String.class);
                        String menuName = itemSnapshot.child("menuName").getValue(String.class);
                        int menuPrice = itemSnapshot.child("menuPrice").getValue(Integer.class);
                        int quantity = itemSnapshot.child("quantity").getValue(Integer.class);

                        OrderDetail.Item item = new OrderDetail.Item(menuId, menuName, menuPrice, quantity);
                        orderedItems.add(item);
                    }

                    OrderDetail orderDetail = new OrderDetail(orderId, orderDate, orderTime, orderedItems, totalPrice);

                    // Display Order Data
                    tvRefNumber.setText(orderDetail.getOrderId());
                    String dateTime = orderDetail.getOrderDate() + ", " + orderDetail.getOrderTime();
                    tvPaymentTime.setText(dateTime);
                    tvAmount.setText(formatToRupiah(orderDetail.getTotalPrice()));

                    // Set up RecyclerView with OrderReceiptAdapter
                    OrderReceiptAdapter adapter = new OrderReceiptAdapter(orderDetail.getOrderedItems(), OrderReceiptActivity.this);
                    recyclerViewReceiptDetails.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors here
            }
        });
    }

    private String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatRupiah.format(amount).replace("Rp", "Rp.").replace(",00", ",-");
    }

    private void generatePDFReceipt() {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Title (Payment Success)
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK); // Text color for title
        titlePaint.setTextSize(18);       // Font size for title
        titlePaint.setTextAlign(Paint.Align.CENTER); // Center alignment
        canvas.drawText("Payment Success", 297, 100, titlePaint); // Centered at the top of the page

        // Labels and Content (Ref OrderId, Payment Time, and Total)
        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.DKGRAY); // Text color for labels
        labelPaint.setTextSize(14);        // Font size for labels

        Paint contentPaint = new Paint();
        contentPaint.setColor(Color.BLACK); // Text color for content
        contentPaint.setTextSize(16);       // Font size for content

        int x = 50;  // X position for left alignment
        int y = 160; // Initial Y position below the title

        // Ref Order Id
        canvas.drawText("Ref Order Id", x, y, labelPaint);
        y += 20;
        canvas.drawText(tvRefNumber.getText().toString(), x, y, contentPaint);
        y += 40;

        // Payment Time
        canvas.drawText("Payment Time", x, y, labelPaint);
        y += 20;
        canvas.drawText(tvPaymentTime.getText().toString(), x, y, contentPaint);
        y += 40;

        // Detail Payment
        canvas.drawText("Detail Payment", x, y, labelPaint);
        y += 30;

        // Ordered items list
        for (OrderDetail.Item item : ((OrderReceiptAdapter) recyclerViewReceiptDetails.getAdapter()).getOrderItems()) {
            String itemDetails = item.getMenuName() + " x" + item.getQuantity() + " - " + formatToRupiah(item.getMenuPrice());
            canvas.drawText(itemDetails, x, y, contentPaint);
            y += 20;
        }

        y += 30; // Space before total

        // Total
        Paint totalPaint = new Paint();
        totalPaint.setColor(Color.BLACK);   // Text color for total
        totalPaint.setTextSize(18);         // Font size for total
        totalPaint.setTextAlign(Paint.Align.LEFT); // Align left
        canvas.drawText("Total: " + tvAmount.getText().toString(), x, y, totalPaint);

        // Finish page
        pdfDocument.finishPage(page);

        // Saving PDF
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File file = new File(directoryPath, "OrderReceipt_" + orderId + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

}
