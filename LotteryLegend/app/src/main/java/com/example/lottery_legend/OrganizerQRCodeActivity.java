package com.example.lottery_legend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerQRCodeActivity extends AppCompatActivity {

    private ImageView imageQrCode;
    private TextView textEventTitle;
    private String eventId;
    private String eventTitle;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_qrcode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        imageQrCode = findViewById(R.id.imageQrCode);
        textEventTitle = findViewById(R.id.textEventTitle);
        MaterialToolbar toolbar = findViewById(R.id.toolbarOrganizerQr);

        eventId = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");

        if (eventTitle != null) {
            textEventTitle.setText(eventTitle);
        }

        if (eventId != null) {
            loadQRCode(eventId);
        } else {
            Toast.makeText(this, "Error: No Event ID found", Toast.LENGTH_SHORT).show();
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Loads the PRE-GENERATED QR code from Firestore.
     */
    private void loadQRCode(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String qrBase64 = documentSnapshot.getString("qrCodeImage");
                        if (qrBase64 != null && !qrBase64.isEmpty()) {
                            // Decode the Base64 string and display it
                            byte[] decodedString = Base64.decode(qrBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            imageQrCode.setImageBitmap(decodedByte);
                        } else {
                            Toast.makeText(this, "No QR Code found for this event", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load QR code", Toast.LENGTH_SHORT).show();
                });
    }
}
