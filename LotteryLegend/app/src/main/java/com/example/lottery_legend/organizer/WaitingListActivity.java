package com.example.lottery_legend.organizer;

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

    public static class WaitingListUser {
        public Entrant entrant;
        public Event.WaitingListEntry entry;

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
    }

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
        
        mapIcon.setOnClickListener(v -> Toast.makeText(this, "Map view coming soon", Toast.LENGTH_SHORT).show());
        btnFilter.setOnClickListener(this::showFilterMenu);
    }

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

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerWaitingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitingListAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

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

    private void promoteUser(WaitingListUser user) {
        String notificationId = db.collection("notifications").document().getId();
        Notification notif = new Notification(
            notificationId, deviceId, user.entrant.getDeviceId(), "ENTRANT",
            eventId, "CO_ORGANIZER_INVITE", "Co-organizer Invitation",
            "You have been invited to be a co-organizer for: " + (eventTitle != null ? eventTitle : "an event"),
            false, Timestamp.now(), "PENDING"
        );

        WriteBatch batch = db.batch();
        batch.set(db.collection("notifications").document(notificationId), notif);
        
        // Basic info for Entrant's sub-collection
        Map<String, Object> summary = new HashMap<>();
        summary.put("notificationId", notificationId);
        summary.put("type", notif.getType());
        summary.put("createdAt", notif.getCreatedAt());
        batch.set(db.collection("entrants").document(user.entrant.getDeviceId()).collection("notifications").document(notificationId), summary);

        batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(this, "Invitation sent", Toast.LENGTH_SHORT).show());
    }

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
                            sendCancellationNotification(user);
                            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show();
                        });
                }
            }
        });
    }

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
