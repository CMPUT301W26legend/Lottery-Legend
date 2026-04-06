package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.MapActivity;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for organizers to view and manage the waiting list of an event.
 * Provides functionality to search, filter by status, view entrant locations on a map,
 * promote entrants to co-organizers, and cancel lottery selections.
 */
public class WaitingListActivity extends AppCompatActivity implements WaitingListAdapter.OnEntrantActionListener {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;
    private String eventTitle;
    
    private RecyclerView recyclerView;
    private WaitingListAdapter adapter;
    private List<WaitingListUser> entrantList = new ArrayList<>();
    private TextView textEntrantCount;
    private EditText editSearch;
    private ImageButton mapIcon;
    private View btnFilter;

    private String currentSearchText = "";
    private String currentStatusFilter = "All";

    /**
     * Helper class that bundles an Entrant model with their corresponding WaitingListEntry.
     */
    public static class WaitingListUser {
        /** The entrant's profile information. */
        public Entrant entrant;
        /** The entrant's participation details for this specific event. */
        public Event.WaitingListEntry entry;

        /**
         * Constructs a new WaitingListUser.
         * @param entrant The Entrant profile.
         * @param entry   The waiting list entry details.
         */
        public WaitingListUser(Entrant entrant, Event.WaitingListEntry entry) {
            this.entrant = entrant;
            this.entry = entry;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiting_list);
        
        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        deviceId = getIntent().getStringExtra("deviceId");

        initViews();
        setupRecyclerView();
        fetchWaitingList();
        setupSearch();

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HOME);
    }

    /**
     * Initializes UI components and sets up the toolbar and window insets.
     */
    private void initViews() {
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarWaitingList);
        toolbar.setNavigationOnClickListener(v -> finish());

        textEntrantCount = findViewById(R.id.textEntrantCount);
        editSearch = findViewById(R.id.editSearchEntrants);
        mapIcon = findViewById(R.id.mapIcon);
        btnFilter = findViewById(R.id.buttonFilter);
        
        mapIcon.setOnClickListener(v -> openEntrantMap());
        btnFilter.setOnClickListener(this::showFilterMenu);
    }

    /**
     * Launches the MapActivity to display the locations of entrants who joined with geolocation.
     */
    private void openEntrantMap() {
        ArrayList<Double> latitudes = new ArrayList<>();
        ArrayList<Double> longitudes = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        for (WaitingListUser user : entrantList) {
            if (user.entry != null && user.entry.getJoinLatitude() != null && user.entry.getJoinLongitude() != null) {
                latitudes.add(user.entry.getJoinLatitude());
                longitudes.add(user.entry.getJoinLongitude());
                names.add(user.entrant.getName() + " (" + user.entry.getParticipationStatus() + ")");
            }
        }

        if (latitudes.isEmpty()) {
            Toast.makeText(this, "No geo-location data available for entrants", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_TITLE, "Entrant Locations");
        intent.putExtra(MapActivity.EXTRA_LATITUDES, latitudes);
        intent.putExtra(MapActivity.EXTRA_LONGITUDES, longitudes);
        intent.putExtra(MapActivity.EXTRA_NAMES, names);
        startActivity(intent);
    }

    /**
     * Displays a popup menu to filter the waiting list by participation status.
     * @param v The view that triggered the menu.
     */
    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        String[] statuses = {"All", "Waiting", "Selected", "Accepted", "Cancelled/Declined", "Not Selected"};
        for (String status : statuses) {
            popup.getMenu().add(status);
        }
        popup.setOnMenuItemClickListener(item -> {
            currentStatusFilter = item.getTitle().toString();
            applyFilters();
            return true;
        });
        popup.show();
    }

    /**
     * Initializes the RecyclerView with a linear layout manager and the custom adapter.
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerWaitingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitingListAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Sets up a snapshot listener for the event document to stay updated with waiting list changes.
     */
    private void fetchWaitingList() {
        if (eventId == null) return;
        db.collection("events").document(eventId).addSnapshotListener((documentSnapshot, error) -> {
            if (error != null || documentSnapshot == null || !documentSnapshot.exists()) return;
            Event event = documentSnapshot.toObject(Event.class);
            if (event != null) {
                this.eventTitle = event.getTitle();
                if (event.getWaitingList() != null) {
                    loadEntrantDetails(event.getWaitingList());
                } else {
                    entrantList.clear();
                    applyFilters();
                }
            }
        });
    }

    /**
     * Fetches detailed profile information for each entrant in the waiting list.
     * @param entries The list of waiting list entries from the event document.
     */
    private void loadEntrantDetails(List<Event.WaitingListEntry> entries) {
        if (entries.isEmpty()) {
            entrantList.clear();
            applyFilters();
            return;
        }
        List<WaitingListUser> tempUsers = new ArrayList<>();
        final int[] count = {0};
        for (Event.WaitingListEntry entry : entries) {
            db.collection("entrants").document(entry.getDeviceId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Entrant entrant = doc.toObject(Entrant.class);
                    tempUsers.add(new WaitingListUser(entrant, entry));
                }
                count[0]++;
                if (count[0] == entries.size()) {
                    entrantList.clear();
                    entrantList.addAll(tempUsers);
                    applyFilters();
                }
            }).addOnFailureListener(e -> {
                count[0]++;
                if (count[0] == entries.size()) {
                    entrantList.clear();
                    entrantList.addAll(tempUsers);
                    applyFilters();
                }
            });
        }
    }

    /**
     * Applies the current search text and status filter to the list of entrants.
     */
    private void applyFilters() {
        List<WaitingListUser> filteredList = new ArrayList<>();
        for (WaitingListUser item : entrantList) {
            boolean matchesName = item.entrant.getName().toLowerCase().contains(currentSearchText.toLowerCase());
            boolean matchesStatus = currentStatusFilter.equals("All") || isStatusMatch(item.entry.getParticipationStatus(), currentStatusFilter);
            if (matchesName && matchesStatus) filteredList.add(item);
        }
        adapter.updateList(filteredList);
        textEntrantCount.setText(String.format(Locale.getDefault(), "%d entrants", filteredList.size()));
    }

    /**
     * Helper to determine if a status matches a filter category.
     */
    private boolean isStatusMatch(String actualStatus, String filterStatus) {
        if (actualStatus == null) actualStatus = "Waiting";
        String s = actualStatus.toLowerCase();
        switch (filterStatus) {
            case "Waiting": return s.equals("waiting");
            case "Selected": return s.equals("invited") || s.equals("selected");
            case "Accepted": return s.equals("accepted") || s.equals("enrolled");
            case "Cancelled/Declined": return s.equals("cancelled") || s.equals("declined");
            case "Not Selected": return s.equals("not selected");
            default: return false;
        }
    }

    /**
     * Configures the search EditText to trigger list filtering on text changes.
     */
    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Callback from adapter when the promote button is clicked. Shows a confirmation dialog.
     */
    @Override
    public void onPromote(WaitingListUser user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_promote_coorganizer, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(dialogView).create();
        dialogView.findViewById(R.id.buttonCancelPromote).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.buttonConfirmPromote).setOnClickListener(v -> {
            promoteUser(user);
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * Callback from adapter when the cancel selection button is clicked. Shows a confirmation dialog.
     */
    @Override
    public void onCancelSelection(WaitingListUser user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_selection, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(dialogView).create();
        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.buttonConfirm).setOnClickListener(v -> {
            cancelSelection(user);
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * Promotes an entrant to a co-organizer by sending them a formal invitation notification.
     * @param user The user to promote.
     */
    private void promoteUser(WaitingListUser user) {
        if (user.entrant == null) return;

        if (!user.entrant.isNotificationsEnabled()) {
            Toast.makeText(this, "Notification denied: user has disabled notifications", Toast.LENGTH_LONG).show();
            return;
        }

        String notificationId = db.collection("notifications").document().getId();
        Notification notif = new Notification(
            notificationId, deviceId, user.entrant.getDeviceId(), "ENTRANT",
            eventId, "CO_ORGANIZER_INVITE", "Co-organizer Invitation",
            "You have been invited to be a co-organizer for: " + (eventTitle != null ? eventTitle : "an event"),
            false, Timestamp.now(), "PENDING"
        );

        WriteBatch batch = db.batch();
        batch.set(db.collection("notifications").document(notificationId), notif);
        
        // Add notification summary to entrant's sub-collection
        Map<String, Object> summary = new HashMap<>();
        summary.put("notificationId", notificationId);
        summary.put("type", notif.getType());
        summary.put("createdAt", notif.getCreatedAt());
        batch.set(db.collection("entrants").document(user.entrant.getDeviceId()).collection("notifications").document(notificationId), summary);

        batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(this, "Invitation sent", Toast.LENGTH_SHORT).show());
    }

    /**
     * Cancels an entrant's lottery selection and updates their status in the waiting list.
     * @param user The user whose selection should be cancelled.
     */
    private void cancelSelection(WaitingListUser user) {
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Event event = doc.toObject(Event.class);
                if (event != null && event.getWaitingList() != null) {
                    List<Event.WaitingListEntry> waitingList = event.getWaitingList();
                    for (Event.WaitingListEntry entry : waitingList) {
                        if (user.entrant.getDeviceId().equals(entry.getDeviceId())) {
                            entry.setParticipationStatus("cancelled");
                            entry.setCancelledAt(Timestamp.now());
                            break;
                        }
                    }
                    db.collection("events").document(eventId).update("waitingList", waitingList)
                        .addOnSuccessListener(aVoid -> {
                            if (user.entrant.isNotificationsEnabled()) {
                                sendCancellationNotification(user);
                            } else {
                                Toast.makeText(this, "Notification denied: user has disabled notifications", Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show();
                        });
                }
            }
        });
    }

    /**
     * Sends a notification to an entrant informing them that their selection has been cancelled.
     * @param user The recipient user.
     */
    private void sendCancellationNotification(WaitingListUser user) {
        String notificationId = db.collection("notifications").document().getId();
        Notification notif = new Notification(
            notificationId, deviceId, user.entrant.getDeviceId(), "ENTRANT",
            eventId, "CANCELLED_MESSAGE", "Selection Cancelled",
            "Your selection for \"" + (eventTitle != null ? eventTitle : "the event") + "\" was cancelled.",
            false, Timestamp.now(), "NONE"
        );

        WriteBatch batch = db.batch();
        batch.set(db.collection("notifications").document(notificationId), notif);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("notificationId", notificationId);
        summary.put("type", notif.getType());
        summary.put("createdAt", notif.getCreatedAt());
        batch.set(db.collection("entrants").document(user.entrant.getDeviceId()).collection("notifications").document(notificationId), summary);
        
        batch.commit();
    }
}
