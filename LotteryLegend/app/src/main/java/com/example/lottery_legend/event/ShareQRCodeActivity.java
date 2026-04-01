package com.example.lottery_legend.event;

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
import com.example.lottery_legend.entrant.NavbarEntrant;
import com.example.lottery_legend.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for entrants to view and share the event QR code.
 */
public class ShareQRCodeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    private TextView textEventTitle;
    private ImageView imageQrCode;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_share_qrcode);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        deviceId = getIntent().getStringExtra("deviceId");

        setupViews();
        fetchEventDetails();

        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.HOME);
    }

    private void setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbarShare);
        toolbar.setNavigationOnClickListener(v -> finish());

        textEventTitle = findViewById(R.id.textEventTitle);
        imageQrCode = findViewById(R.id.imageQrCode);
    }

    private void fetchEventDetails() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            Event event = documentSnapshot.toObject(Event.class);
            if (event != null) {
                textEventTitle.setText(event.getTitle());
                displayQrCode(event.getQrCodeImage());
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayQrCode(String base64String) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    imageQrCode.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error decoding QR code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No QR code available for this event", Toast.LENGTH_SHORT).show();
        }
    }
}
