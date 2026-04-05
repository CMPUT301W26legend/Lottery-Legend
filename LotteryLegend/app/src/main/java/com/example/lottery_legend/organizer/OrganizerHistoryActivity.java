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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerHistoryActivity displays a list of all events created or co-organized by the current organizer.
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

        // Default: Load Organizer events
        loadOrganizerEvents();
    }

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

    private void setupNavbar() {
        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HISTORY);
    }

    private void removeListener() {
        if (currentListener != null) {
            currentListener.remove();
            currentListener = null;
        }
    }

    private void loadOrganizerEvents() {
        if (deviceId == null) return;
        removeListener();
        eventList.clear();
        adapter.notifyDataSetChanged();

        currentListener = db.collection("organizers")
                .document(deviceId)
                .collection("createdEvents")
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