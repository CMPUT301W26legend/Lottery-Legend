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
 * Displays summary statistics and a list of events created by the organizer.
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

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HOME);
    }

    private void initViews() {
        textActiveEventsCount = findViewById(R.id.textActiveEventsCount);
        textClosedEventsCount = findViewById(R.id.textClosedEventsCount);
        textPendingLotteriesCount = findViewById(R.id.textPendingLotteriesCount);
        textTotalEventsCount = findViewById(R.id.textTotalEventsCount);
        buttonCreateEvent = findViewById(R.id.ButtonCreateEvent);
        recyclerViewOrganizerEvents = findViewById(R.id.recyclerViewOrganizerEvents);
    }

    private void setupRecyclerView() {
        recyclerViewOrganizerEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerEventAdapter(eventList, deviceId);
        recyclerViewOrganizerEvents.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainActivity.this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Fetches events created by this organizer and updates statistics and the event list.
     * Uses a snapshot listener for real-time updates.
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

                            // Define pending lottery: draw time has passed but status is not "drawn"
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
     * Updates the summary dashboard cards with calculated counts.
     */
    private void updateStats(int active, int closed, int pending, int total) {
        textActiveEventsCount.setText(String.valueOf(active));
        textClosedEventsCount.setText(String.valueOf(closed));
        textPendingLotteriesCount.setText(String.valueOf(pending));
        textTotalEventsCount.setText(String.valueOf(total));
    }
}
