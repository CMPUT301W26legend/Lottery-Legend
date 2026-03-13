package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerHistoryActivity displays a list of all events created by the current organizer.
 * Clicking an event navigates to the OrganizerEventDetailsActivity.
 */
public class OrganizerHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private List<Event> organizerEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_history);

        // System UI configuration
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore and Device ID
        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        // Set up the Organizer Navigation Bar using the common utility class
        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HISTORY);

        // Initialize RecyclerView and its layout manager
        recyclerView = findViewById(R.id.recyclerOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize the specialized OrganizerEventAdapter
        // Navigation is handled directly inside the adapter
        adapter = new OrganizerEventAdapter(organizerEvents, deviceId);
        
        recyclerView.setAdapter(adapter);

        // Fetch events from Firestore
        loadOrganizerEvents();

        // Configure "Create Event" button navigation
        Button btnCreateEvent = findViewById(R.id.createEventButton);
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerHistoryActivity.this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Queries Firestore for all events associated with the current organizer's device ID.
     * Uses a snapshot listener for real-time updates.
     */
    private void loadOrganizerEvents() {
        if (deviceId == null) return;

        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("OrganizerHistory", "Error fetching events", error);
                        return;
                    }

                    organizerEvents.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            organizerEvents.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
