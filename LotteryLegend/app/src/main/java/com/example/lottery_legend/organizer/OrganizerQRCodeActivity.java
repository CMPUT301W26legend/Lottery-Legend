package com.example.lottery_legend.organizer;

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

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for organizers to view and share the QR code for a specific event.
 */
public class OrganizerQRCodeActivity extends AppCompatActivity {

    private ImageView imageQrCode;
    private TextView textEventTitle;
    private String eventId;
    private String eventTitle;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves event data from the intent, and triggers the QR code loading process.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
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
     * Fetches the pre-generated QR code Base64 string from the "events" collection in Firestore.
     * Decodes the string into a Bitmap and updates the ImageView.
     *
     * @param eventId The unique identifier of the event whose QR code should be loaded.
     */
    private void loadQRCode(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            // Retrieve the Base64 encoded image string from the document
                            String qrBase64 = event.getQrCodeImage();
                            if (qrBase64 != null && !qrBase64.isEmpty()) {
                                // Decode the Base64 string into a byte array
                                byte[] decodedString = Base64.decode(qrBase64, Base64.DEFAULT);
                                // Convert the byte array into a Bitmap and display it
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imageQrCode.setImageBitmap(decodedByte);
                            } else {
                                Toast.makeText(this, "No QR Code found for this event", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle potential network or Firestore errors
                    Toast.makeText(this, "Failed to load QR code", Toast.LENGTH_SHORT).show();
                });
    }
}
