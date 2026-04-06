package com.example.lottery_legend.organizer;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for organizers to send bulk notifications to different groups of entrants.
 * Strictly respects notification permissions and uses defined enum types.
 */
public class SendNotificationActivity extends AppCompatActivity {

    private String eventId;
    private String deviceId;
    private FirebaseFirestore db;
    private String selectedTab = "waiting"; // "waiting", "selected", "cancelled"

    private MaterialButton btnWaitingList, btnSelected, btnCancelled, btnNotifyWinLoss;
    private TextInputEditText editMessageTitle, editMessageBody;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventId = getIntent().getStringExtra("eventId");
        deviceId = getIntent().getStringExtra("deviceId");

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        updateTabSelection("waiting");

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HISTORY);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSendNotification);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnWaitingList = findViewById(R.id.btnWaitingList);
        btnSelected = findViewById(R.id.btnSelected);
        btnCancelled = findViewById(R.id.btnCancelled);
        btnNotifyWinLoss = findViewById(R.id.btnNotifyWinLoss);

        editMessageTitle = findViewById(R.id.editMessageTitle);
        editMessageBody = findViewById(R.id.editMessageBody);
        btnSend = findViewById(R.id.btnSendToRecipients);
    }

    private void setupListeners() {
        btnWaitingList.setOnClickListener(v -> updateTabSelection("waiting"));
        btnSelected.setOnClickListener(v -> updateTabSelection("selected"));
        btnCancelled.setOnClickListener(v -> updateTabSelection("cancelled"));

        btnSend.setOnClickListener(v -> {
            String title = editMessageTitle.getText().toString().trim();
            String body = editMessageBody.getText().toString().trim();

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            sendNotificationToGroup(title, body);
        });

        btnNotifyWinLoss.setOnClickListener(v -> handleNotifyWinLoss());
    }

    private void updateTabSelection(String tab) {
        selectedTab = tab;
        int activeColor = Color.WHITE;
        int inactiveColor = Color.parseColor("#4F7DF0");
        int activeTextColor = Color.parseColor("#2563EB");
        int inactiveTextColor = Color.WHITE;

        btnWaitingList.setBackgroundTintList(ColorStateList.valueOf(tab.equals("waiting") ? activeColor : inactiveColor));
        btnWaitingList.setTextColor(tab.equals("waiting") ? activeTextColor : inactiveTextColor);

        btnSelected.setBackgroundTintList(ColorStateList.valueOf(tab.equals("selected") ? activeColor : inactiveColor));
        btnSelected.setTextColor(tab.equals("selected") ? activeTextColor : inactiveTextColor);

        btnCancelled.setBackgroundTintList(ColorStateList.valueOf(tab.equals("cancelled") ? activeColor : inactiveColor));
        btnCancelled.setTextColor(tab.equals("cancelled") ? activeTextColor : inactiveTextColor);
    }

    /**
     * Logic for notifying all winners (SELECTED/ACCEPTED) and all others (Losers).
     * Rule 10: Idempotency check.
     * Rule 11: Marks event as finalized.
     */
    private void handleNotifyWinLoss() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null) {
                    // IDEMPOTENCY CHECK: Do not run if already finalized
                    if ("finalized".equals(event.getStatus())) {
                        Toast.makeText(this, "Event is already finalized. Win/Loss notifications have already been sent.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (event.getWaitingList() != null) {
                        finalizeAndNotify(event);
                    }
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch event data", Toast.LENGTH_SHORT).show());
    }

    private void finalizeAndNotify(Event event) {
        Timestamp now = Timestamp.now();
        List<Event.WaitingListEntry> waitingList = event.getWaitingList();
        List<String> deviceIds = new ArrayList<>();
        for (Event.WaitingListEntry entry : waitingList) {
            deviceIds.add(entry.getDeviceId());
        }

        if (deviceIds.isEmpty()) {
            Toast.makeText(this, "No participants in waiting list.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch entrants to check notificationsEnabled (Rule 13)
        db.collection("entrants").whereIn("deviceId", deviceIds).get().addOnSuccessListener(querySnapshot -> {
            Map<String, Boolean> notificationPrefs = new HashMap<>();
            for (var doc : querySnapshot.getDocuments()) {
                Entrant entrant = doc.toObject(Entrant.class);
                if (entrant != null) {
                    notificationPrefs.put(entrant.getDeviceId(), entrant.isNotificationsEnabled());
                }
            }

            WriteBatch batch = db.batch();
            int winCount = 0;
            int lossCount = 0;

            for (Event.WaitingListEntry entry : waitingList) {
                String status = entry.getParticipationStatus() != null ? entry.getParticipationStatus().toLowerCase() : "";
                // Rule 9: Winners are SELECTED or ACCEPTED
                boolean isWinner = status.equals("selected") || status.equals("accepted");
                
                // Rule 6 & 8: Strictly respect notificationsEnabled
                boolean canNotify = notificationPrefs.getOrDefault(entry.getDeviceId(), true);

                if (isWinner) {
                    entry.setFinalResult("WIN");
                    if (canNotify) {
                        // Rule 2: Use LOTTERY_WIN enum
                        addNotificationToBatch(batch, entry.getDeviceId(), "LOTTERY_WIN", 
                            "Congratulations! You Won", 
                            "You have been selected as a final attendee for: " + event.getTitle(), now);
                    }
                    winCount++;
                } else {
                    entry.setFinalResult("LOSS");
                    if (canNotify) {
                        // Rule 2: Use LOTTERY_LOSE enum
                        addNotificationToBatch(batch, entry.getDeviceId(), "LOTTERY_LOSE", 
                            "Lottery Result Update", 
                            "We regret to inform you that you were not selected for the final attendee list for: " + event.getTitle(), now);
                    }
                    lossCount++;
                }
                entry.setUpdatedAt(now);
            }

            // Update event status to finalized (Rule 11) - Idempotency protection
            DocumentReference eventRef = db.collection("events").document(eventId);
            batch.update(eventRef, "status", "finalized", "waitingList", waitingList, "updatedAt", now);

            final int finalWinCount = winCount;
            final int finalLossCount = lossCount;
            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Finalized! Winners: " + finalWinCount + ", Losers: " + finalLossCount, Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Finalization failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Helper to create notification document according to Schema (Rule 1).
     */
    private void addNotificationToBatch(WriteBatch batch, String recipientId, String type, String title, String message, Timestamp now) {
        DocumentReference notifRef = db.collection("notifications").document();
        Map<String, Object> notification = new HashMap<>();
        notification.put("notificationId", notifRef.getId());
        notification.put("recipientId", recipientId);
        notification.put("senderId", deviceId);
        notification.put("recipientType", "ENTRANT"); // Rule 4
        notification.put("eventId", eventId);
        notification.put("type", type); // Rule 2
        notification.put("title", title);
        notification.put("message", message);
        notification.put("isRead", false);
        notification.put("createdAt", now);
        notification.put("actionStatus", "NONE"); // Rule 3
        batch.set(notifRef, notification);
    }

    private void sendNotificationToGroup(String title, String body) {
        if (eventId == null) return;

        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null && event.getWaitingList() != null) {
                    List<Event.WaitingListEntry> recipients = new ArrayList<>();
                    for (Event.WaitingListEntry entry : event.getWaitingList()) {
                        if (isTargetStatus(entry.getParticipationStatus())) {
                            recipients.add(entry);
                        }
                    }

                    if (recipients.isEmpty()) {
                        Toast.makeText(this, "No recipients found for this group", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    performBatchSend(recipients, title, body);
                }
            }
        });
    }

    private boolean isTargetStatus(String status) {
        if (status == null) status = "waiting";
        String s = status.toLowerCase();
        switch (selectedTab) {
            case "waiting":
                return true; // All entrants in the list
            case "selected":
                return s.equals("selected") || s.equals("accepted") ;
            case "cancelled":
                return s.equals("cancelled") || s.equals("declined");
            default:
                return false;
        }
    }

    private void performBatchSend(List<Event.WaitingListEntry> recipients, String title, String body) {
        List<String> deviceIds = new ArrayList<>();
        for (Event.WaitingListEntry r : recipients) {
            deviceIds.add(r.getDeviceId());
        }

        // Rule 13: Check notification preference
        db.collection("entrants").whereIn("deviceId", deviceIds).get().addOnSuccessListener(querySnapshot -> {
            Map<String, Boolean> notificationPrefs = new HashMap<>();
            for (var doc : querySnapshot.getDocuments()) {
                Entrant entrant = doc.toObject(Entrant.class);
                if (entrant != null) {
                    notificationPrefs.put(entrant.getDeviceId(), entrant.isNotificationsEnabled());
                }
            }

            WriteBatch batch = db.batch();
            Timestamp now = Timestamp.now();
            int sentCount = 0;

            // Rule 2: Map group to valid notification enum
            String type;
            switch (selectedTab) {
                case "waiting": type = "WAITLIST_MESSAGE"; break;
                case "selected": type = "SELECTED_MESSAGE"; break;
                case "cancelled": type = "CANCELLED_MESSAGE"; break;
                default: type = "GENERAL";
            }

            for (Event.WaitingListEntry recipient : recipients) {
                // Rule 6: Skip ONLY if disabled
                boolean canNotify = notificationPrefs.getOrDefault(recipient.getDeviceId(), true);
                if (canNotify) {
                    addNotificationToBatch(batch, recipient.getDeviceId(), type, title, body, now);
                    sentCount++;
                }
            }

            if (sentCount == 0) {
                Toast.makeText(this, "No notifications sent (recipients have disabled notifications).", Toast.LENGTH_SHORT).show();
                return;
            }

            final int finalSentCount = sentCount;
            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Notifications sent to " + finalSentCount + " recipients", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to send notifications", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
