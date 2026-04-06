package com.example.lottery_legend.event;

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

import com.example.lottery_legend.R;
import com.example.lottery_legend.entrant.NavbarEntrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

/**
 * Activity that handles QR code scanning using the ZXing library.
 * It allows entrants to scan event QR codes to quickly view details and join waiting lists.
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

        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.SCAN);
    }

    /**
     * Callback method from ZXing when a barcode is successfully scanned.
     * Extracts the raw text and triggers a database check.
     * @param result The result object containing the raw text of the scanned barcode.
     */
    @Override
    public void barcodeResult(BarcodeResult result) {
        if (result.getText() != null) {
            // Pause scanning to prevent multiple triggers while processing the current result
            barcodeView.pause();

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
                        // Valid event found, navigate to details and pass relevant identifiers
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
                    // Handle potential network or Firestore errors
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    barcodeView.resume();
                });
    }

    /**
     * Resumes the camera and scanning process when the activity is brought to the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    /**
     * Pauses the camera and scanning process when the activity is moved to the background.
     */
    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    /**
     * Callback for potential points detected during the scanning process.
     * Can be used for providing visual feedback (e.g., dots over detected patterns).
     * @param resultPoints List of points detected by the scanner.
     */
    @Override
    public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
        // Implementation for UI feedback on detected points could be added here
    }
}
