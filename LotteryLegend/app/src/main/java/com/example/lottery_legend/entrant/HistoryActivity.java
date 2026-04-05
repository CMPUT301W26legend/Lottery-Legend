package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Activity for entrants to view their event participation history.
 * Groups events by participation results using a TabLayout.
 */
public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    // Tab Group Names
    private static final String TAB_WAITING = "Waiting";
    private static final String TAB_ACCEPTED = "Accepted";
    private static final String TAB_NOT_SELECTED = "Not Selected";
    private static final String TAB_CANCELLED = "Cancelled";

    private FirebaseFirestore db;
    private String deviceId;

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private FrameLayout layoutNotification;
    private TextView tvNotificationBadge;

    private final List<Event> allJoinedEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();
    private String currentSelectedTab = TAB_WAITING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTabs();
        setupNavbar();
        setupNotificationBadge();

        loadJoinedEvents();
    }

    private void initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbarHistory);
        tabLayout = findViewById(R.id.tabLayoutHistory);
        recyclerView = findViewById(R.id.recyclerHistory);
        layoutNotification = findViewById(R.id.layoutNotification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("History");
        }
        
        layoutNotification.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, NotificationActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    private void setupNotificationBadge() {
        if (deviceId == null) return;
        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        tvNotificationBadge.setVisibility(View.GONE);
                        return;
                    }
                    int count = value.size();
                    if (count > 0) {
                        tvNotificationBadge.setVisibility(View.VISIBLE);
                        tvNotificationBadge.setText(String.valueOf(count));
                    } else {
                        tvNotificationBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(filteredEvents, deviceId);
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentSelectedTab = TAB_WAITING; break;
                    case 1: currentSelectedTab = TAB_ACCEPTED; break;
                    case 2: currentSelectedTab = TAB_NOT_SELECTED; break;
                    case 3: currentSelectedTab = TAB_CANCELLED; break;
                }
                applyTabFilter(currentSelectedTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupNavbar() {
        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.HISTORY);
    }

    /**
     * Queries Firestore for all events where the current entrant is in the waiting list.
     */
    private void loadJoinedEvents() {
        // We listen for real-time updates so the history reflects changes immediately
        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error loading joined events", error);
                Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
                return;
            }

            allJoinedEvents.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    if (event != null && isEntrantInEvent(event)) {
                        allJoinedEvents.add(event);
                    }
                }
            }
            applyTabFilter(currentSelectedTab);
        });
    }

    /**
     * Checks if the current entrant device ID exists in the event's waiting list.
     */
    private boolean isEntrantInEvent(Event event) {
        if (event.getWaitingList() == null) return false;
        for (Event.WaitingListEntry entry : event.getWaitingList()) {
            if (Objects.equals(entry.getDeviceId(), deviceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filters the loaded events based on the selected tab and updates the RecyclerView.
     */
    private void applyTabFilter(String selectedTab) {
        filteredEvents.clear();
        for (Event event : allJoinedEvents) {
            String status = determineEntrantStatus(event);
            if (matchesTab(status, selectedTab)) {
                filteredEvents.add(event);
            }
        }
        adapter.updateList(filteredEvents);
    }

    /**
     * Logic to map an event's entrant record to a specific display status.
     */
    private String determineEntrantStatus(Event event) {
        if (event.getWaitingList() == null) return "Unknown";

        for (Event.WaitingListEntry entry : event.getWaitingList()) {
            if (Objects.equals(entry.getDeviceId(), deviceId)) {
                String fsStatus = entry.getParticipationStatus();
                if (fsStatus == null) return "Waiting";

                switch (fsStatus.toLowerCase()) {
                    case "waiting": return "Waiting";
                    case "invited": return "Waiting Response";
                    case "accepted":
                    case "enrolled": return "Accepted";
                    case "not_selected":
                    case "rejected": return "Not Selected";
                    case "cancelled": return "Cancelled";
                    case "declined": return "Declined";
                    default: return "Waiting";
                }
            }
        }
        return "Unknown";
    }

    /**
     * Maps computed display status to the appropriate tab grouping.
     */
    private boolean matchesTab(String status, String tabName) {
        switch (tabName) {
            case TAB_WAITING:
                return "Waiting".equals(status) || "Waiting Response".equals(status);
            case TAB_ACCEPTED:
                return "Accepted".equals(status);
            case TAB_NOT_SELECTED:
                return "Not Selected".equals(status);
            case TAB_CANCELLED:
                return "Cancelled".equals(status) || "Declined".equals(status);
            default:
                return false;
        }
    }
}
