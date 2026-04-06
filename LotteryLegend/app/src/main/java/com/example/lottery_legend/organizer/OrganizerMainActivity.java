package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The main dashboard for users in the Organizer role.
 * Displays summary statistics (active events, closed events, pending lotteries) 
 * and a list of events created by the current organizer.
 */
public class OrganizerMainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    
    private TextView textActiveEventsCount, textClosedEventsCount, textPendingLotteriesCount, textTotalEventsCount;
    private MaterialButton buttonCreateEvent;
    private RecyclerView recyclerViewOrganizerEvents;
    private OrganizerEventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        initViews();
        setupRecyclerView();
        setupListeners();
        fetchOrganizerEvents();

        // Setup the bottom navigation bar
        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HOME);
    }

    /**
     * Initializes UI references from the layout.
     */
    private void initViews() {
        textActiveEventsCount = findViewById(R.id.textActiveEventsCount);
        textClosedEventsCount = findViewById(R.id.textClosedEventsCount);
        textPendingLotteriesCount = findViewById(R.id.textPendingLotteriesCount);
        textTotalEventsCount = findViewById(R.id.textTotalEventsCount);
        buttonCreateEvent = findViewById(R.id.ButtonCreateEvent);
        recyclerViewOrganizerEvents = findViewById(R.id.recyclerViewOrganizerEvents);
    }

    /**
     * Configures the RecyclerView for displaying the organizer's events.
     */
    private void setupRecyclerView() {
        recyclerViewOrganizerEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerEventAdapter(eventList, deviceId);
        recyclerViewOrganizerEvents.setAdapter(adapter);
    }

    /**
     * Sets up click listeners for buttons.
     */
    private void setupListeners() {
        buttonCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainActivity.this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Fetches events created by this organizer from Firestore and updates dashboard statistics.
     * Listens for real-time updates using a snapshot listener.
     */
    private void fetchOrganizerEvents() {
        if (deviceId == null) return;

        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        eventList.clear();
                        int activeCount = 0;
                        int closedCount = 0;
                        int pendingLotteryCount = 0;
                        Timestamp now = Timestamp.now();

                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            eventList.add(event);

                            String status = event.getStatus();
                            if ("open".equalsIgnoreCase(status)) {
                                activeCount++;
                            } else if ("closed".equalsIgnoreCase(status)) {
                                closedCount++;
                            }

                            // A pending lottery is an event where the draw time has passed but the draw hasn't happened.
                            if (event.getDrawAt() != null && event.getDrawAt().compareTo(now) <= 0 && !"drawn".equalsIgnoreCase(status)) {
                                pendingLotteryCount++;
                            }
                        }

                        updateStats(activeCount, closedCount, pendingLotteryCount, eventList.size());
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Updates the text displays for the summary statistics cards.
     * @param active  Number of active events.
     * @param closed  Number of closed events.
     * @param pending Number of pending lotteries.
     * @param total   Total number of events created.
     */
    private void updateStats(int active, int closed, int pending, int total) {
        textActiveEventsCount.setText(String.valueOf(active));
        textClosedEventsCount.setText(String.valueOf(closed));
        textPendingLotteriesCount.setText(String.valueOf(pending));
        textTotalEventsCount.setText(String.valueOf(total));
    }
}
