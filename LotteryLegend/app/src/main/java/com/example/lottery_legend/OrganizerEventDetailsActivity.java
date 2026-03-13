package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private String eventId;
    private String eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components from the layout
        MaterialToolbar toolbar = findViewById(R.id.toolbarOrganizerDetails);
        ImageButton shareIcon = findViewById(R.id.shareIcon);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        // Get Event data passed from the previous activity (e.g., OrganizerHistoryActivity)
        eventId = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");

        if (eventTitle != null) {
            toolbarTitle.setText("Details: " + eventTitle);
        }

        // Back navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        /**
         * Navigates to OrganizerQRCodeActivity to display the event's unique QR code.
         * Requirement: "once click the share button on event details will lead to the QRcodeActivity"
         */
        shareIcon.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerQRCodeActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("eventTitle", eventTitle);
                startActivity(intent);
            }
        });
    }
}