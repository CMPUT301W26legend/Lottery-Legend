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

/**
 * Activity for organizers to view the details of a specific event they have created.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private String eventId;
    private String eventTitle;

    /**
     * Called when the activity is starting.
     * Initializes the UI, retrieves event data from the intent, and sets up navigation and interaction logic.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
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

        MaterialToolbar toolbar = findViewById(R.id.toolbarOrganizerDetails);
        ImageButton shareIcon = findViewById(R.id.shareIcon);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        // Get Event data passed from the previous activity (e.g., OrganizerHistoryActivity)
        eventId = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");

        if (eventTitle != null) {
            toolbarTitle.setText("Details: " + eventTitle);
        }

        // Handle back navigation button click
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
