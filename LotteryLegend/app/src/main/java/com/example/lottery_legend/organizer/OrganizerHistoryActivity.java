package com.example.lottery_legend.organizer;

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

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerHistoryActivity displays a list of all events created by the current organizer.
 */
public class OrganizerHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private List<Event> organizerEvents = new ArrayList<>();

    /**
     * Called when the activity is first created.
     * Initializes the UI, configures Firestore integration, sets up the navigation bar,
     * and prepares the RecyclerView for event display.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
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

        // Initialize Firestore instance and retrieve the device ID from the starting intent
        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HISTORY);

        recyclerView = findViewById(R.id.recyclerOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrganizerEventAdapter(organizerEvents, deviceId);
        recyclerView.setAdapter(adapter);

        // Begin fetching events from the "events" collection
        loadOrganizerEvents();

        // Configure the "Create Event" button to navigate to the event creation screen
        Button btnCreateEvent = findViewById(R.id.createEventButton);
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerHistoryActivity.this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Queries the Firestore "events" collection for documents where "organizerId" matches the current device ID.
     * Implements a real-time snapshot listener to ensure the UI updates immediately when event data
     * is modified in the cloud.
     */
    private void loadOrganizerEvents() {
        if (deviceId == null) return;

        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("OrganizerHistory", "Error fetching events from Firestore", error);
                        return;
                    }

                    // Clear the local cache to avoid duplicates before repopulating
                    organizerEvents.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            // Deserialize the Firestore document into an Event object
                            Event event = doc.toObject(Event.class);
                            organizerEvents.add(event);
                        }
                    }
                    // Notify the adapter that the data set has changed to trigger a UI refresh
                    adapter.notifyDataSetChanged();
                });
    }
}
