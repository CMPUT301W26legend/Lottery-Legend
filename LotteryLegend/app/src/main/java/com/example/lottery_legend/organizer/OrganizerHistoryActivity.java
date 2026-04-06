package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a history of events associated with the organizer.
 * It uses tabs to switch between events created by the user and events where the user is a co-organizer.
 */
public class OrganizerHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();
    private TabLayout tabLayout;
    private ListenerRegistration currentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        initViews();
        setupTabs();
        setupNavbar();

        // Default: Load events created by this organizer
        loadOrganizerEvents();
    }

    /**
     * Initializes the UI components and sets up the RecyclerView.
     */
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayoutOrganizerHistory);
        recyclerView = findViewById(R.id.recyclerOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerEventAdapter(eventList, deviceId);
        recyclerView.setAdapter(adapter);

        Button btnCreateEvent = findViewById(R.id.createEventButton);
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Sets up the TabLayout with listeners to switch between "My Events" and "Co-organized" views.
     */
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadOrganizerEvents();
                } else {
                    loadCoOrganizerEvents();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Initializes the bottom navigation bar.
     */
    private void setupNavbar() {
        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HISTORY);
    }

    /**
     * Removes the current Firestore real-time listener if it exists.
     */
    private void removeListener() {
        if (currentListener != null) {
            currentListener.remove();
            currentListener = null;
        }
    }

    /**
     * Queries and listens for events where the current user is the primary organizer.
     * Updates the list in real-time.
     */
    private void loadOrganizerEvents() {
        if (deviceId == null) return;
        removeListener();
        eventList.clear();
        adapter.notifyDataSetChanged();

        currentListener = db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    eventList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Queries and listens for events where the current user is listed as a co-organizer.
     * Since co-organizers are in a sub-collection, this fetches all events and filters client-side 
     * while the tab is active.
     */
    private void loadCoOrganizerEvents() {
        if (deviceId == null) return;
        removeListener();
        eventList.clear();
        adapter.notifyDataSetChanged();

        currentListener = db.collection("events")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    eventList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            if (event != null && !deviceId.equals(event.getOrganizerId())) {
                                // Check the coOrganizers sub-collection for the current user's ID
                                doc.getReference().collection("coOrganizers").document(deviceId).get()
                                        .addOnSuccessListener(coDoc -> {
                                            if (coDoc.exists() && tabLayout.getSelectedTabPosition() == 1) {
                                                if (!eventList.contains(event)) {
                                                    eventList.add(event);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
