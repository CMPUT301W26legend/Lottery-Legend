package com.example.lottery_legend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

/**
 * ScanActivity handles QR code scanning
 * Reference: https://github.com/journeyapps/zxing-android-embedded
 */
public class ScanActivity extends AppCompatActivity implements BarcodeCallback {

    private CompoundBarcodeView barcodeView;
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");
        barcodeView = findViewById(R.id.barcodeScannerView);
        ImageButton closeButton = findViewById(R.id.closeButton);

        // Start scanning
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(this);

        // Set up the close button to return to the previous screen
        closeButton.setOnClickListener(v -> finish());

        setupNavbar();
    }

    @Override
    public void barcodeResult(BarcodeResult result) {
        if (result.getText() != null) {
            // Pause scanning to prevent multiple dialogs from opening
            barcodeView.pause();

            // Get the scanned ID and check it in the database
            String scannedId = result.getText();
            checkDatabase(scannedId);
        }
    }

    // Check if the scanned ID to get the event details exists in the database
    private void checkDatabase(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Intent intent = new Intent(this, EventDetailsActivity.class);
                        intent.putExtra("eventId", eventId);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                        barcodeView.resume();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    barcodeView.resume();
                });
    }

    private void setupNavbar() {
        View navbar = findViewById(R.id.navbar);
        if (navbar == null) return;

        ImageView imgScan = navbar.findViewById(R.id.imageNavScan);
        TextView txtScan = navbar.findViewById(R.id.textNavScan);
        imgScan.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));
        txtScan.setTextColor(Color.parseColor("#2563EB"));

        navbar.findViewById(R.id.navHome).setOnClickListener(v -> finish());
    }

    // Manage camera resources
    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
        // handle UI feedback for detected points
    }
}
