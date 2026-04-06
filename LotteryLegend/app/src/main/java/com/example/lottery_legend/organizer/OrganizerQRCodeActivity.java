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
 * Activity for organizers to view the QR code for a specific event.
 * Retrieves the pre-generated QR code image from Firestore and displays it.
 */
public class OrganizerQRCodeActivity extends AppCompatActivity {

    private ImageView imageQrCode;
    private TextView textEventTitle;
    private String eventId;
    private String eventTitle;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, sets up the toolbar, and triggers the loading of the QR code.
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

        NavbarOrganizer.setup(this, getIntent().getStringExtra("deviceId"), NavbarOrganizer.Tab.HOME);
    }

    /**
     * Fetches the Base64 encoded QR code image from the event's Firestore document.
     * @param eventId The unique identifier of the event.
     */
    private void loadQRCode(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            String qrBase64 = event.getQrCodeImage();
                            if (qrBase64 != null && !qrBase64.isEmpty()) {
                                byte[] decodedString = Base64.decode(qrBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imageQrCode.setImageBitmap(decodedByte);
                            } else {
                                Toast.makeText(this, "No QR Code found for this event", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load QR code", Toast.LENGTH_SHORT).show();
                });
    }
}
