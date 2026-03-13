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
 * Activity that handles QR code scanning using the ZXing library.
 *
 * <p>Reference: https://github.com/journeyapps/zxing-android-embedded</p>
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

        // Configure the scanner to start scanning immediately without status text
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(this);

        // Set up the close button to finish the activity and return to the previous screen
        closeButton.setOnClickListener(v -> finish());

        // Initialize the navigation bar highlighting
        setupNavbar();
    }

    /**
     * Callback method from ZXing when a barcode is successfully scanned.
     * @param result The result object containing the raw text of the scanned barcode.
     */
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

    /**
     * Verifies if the scanned event ID exists in the Firestore database.
     * If valid, navigates to the EventDetailsActivity; otherwise, resumes scanning.
     *
     * @param eventId The event ID retrieved from the scanned QR code.
     */
    private void checkDatabase(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Valid event found, navigate to details and pass relevant IDs
                        Intent intent = new Intent(this, EventDetailsActivity.class);
                        intent.putExtra("eventId", eventId);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    } else {
                        // Invalid QR code (not matching any event in DB)
                        Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                        barcodeView.resume();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle potential network or permission errors
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    barcodeView.resume();
                });
    }

    /**
     * Configures the navigation bar by highlighting the Scan section.
     * In this activity, the home tab acts as a "back" button.
     */
    private void setupNavbar() {
        View navbar = findViewById(R.id.navbar);
        if (navbar == null) return;

        // Highlight the Scan icon and text to show it as the current active mode
        ImageView imgScan = navbar.findViewById(R.id.imageNavScan);
        TextView txtScan = navbar.findViewById(R.id.textNavScan);
        imgScan.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));
        txtScan.setTextColor(Color.parseColor("#2563EB"));

        // Use the home icon as a quick exit back to the main screen
        navbar.findViewById(R.id.navHome).setOnClickListener(v -> finish());
    }

    /**
     * Manages camera resources when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    /**
     * Manages camera resources and stops scanning when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    /**
     * Callback for potential points detected during the scanning process.
     * Used for providing visual feedback (e.g., dots over detected patterns).
     * @param resultPoints List of points detected by the scanner.
     */
    @Override
    public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
        // handle UI feedback for detected points if needed
    }
}
