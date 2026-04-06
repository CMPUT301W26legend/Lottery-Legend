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
import java.util.Locale;
import java.util.Map;

/**
 * Activity for organizers to send targeted notifications to groups of event participants.
 * Supports notifying the entire waiting list, only selected/accepted entrants, or cancelled/declined entrants.
 * Also handles the final lottery results (Win/Loss notifications) and event finalization.
 */
public class SendNotificationActivity extends AppCompatActivity {

    private String eventId;
    private String deviceId;
    private FirebaseFirestore db;
    /** Currently selected recipient group tab ("waiting", "selected", "cancelled"). */
    private String selectedTab = "waiting";

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

    /**
     * Initializes UI components and sets up the toolbar.
     */
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

    /**
     * Configures click listeners for tab buttons and the send button.
     */
    private void setupListeners() {
        btnWaitingList.setOnClickListener(v -> updateTabSelection("waiting"));
        btnSelected.setOnClickListener(v -> updateTabSelection("selected"));
        btnCancelled.setOnClickListener(v -> updateTabSelection("cancelled"));

        btnSend.setOnClickListener(v -> {
            String title = editMessageTitle.getText() != null
                    ? editMessageTitle.getText().toString().trim()
                    : "";
            String body = editMessageBody.getText() != null
                    ? editMessageBody.getText().toString().trim()
                    : "";

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            sendNotificationToGroup(title, body);
        });

        btnNotifyWinLoss.setOnClickListener(v -> handleNotifyWinLoss());
    }

    /**
     * Updates the visual state of the group selection tabs.
     * @param tab The ID of the tab that was selected.
     */
    private void updateTabSelection(String tab) {
        selectedTab = tab;

        int activeColor = Color.WHITE;
        int inactiveColor = Color.parseColor("#4F7DF0");
        int activeTextColor = Color.parseColor("#2563EB");
        int inactiveTextColor = Color.WHITE;

        btnWaitingList.setBackgroundTintList(
                ColorStateList.valueOf(tab.equals("waiting") ? activeColor : inactiveColor)
        );
        btnWaitingList.setTextColor(tab.equals("waiting") ? activeTextColor : inactiveTextColor);

        btnSelected.setBackgroundTintList(
                ColorStateList.valueOf(tab.equals("selected") ? activeColor : inactiveColor)
        );
        btnSelected.setTextColor(tab.equals("selected") ? activeTextColor : inactiveTextColor);

        btnCancelled.setBackgroundTintList(
                ColorStateList.valueOf(tab.equals("cancelled") ? activeColor : inactiveColor)
        );
        btnCancelled.setTextColor(tab.equals("cancelled") ? activeTextColor : inactiveTextColor);
    }

    /**
     * Entry point for sending final win/loss notifications.
     * Fetches current event data and validates that the event hasn't already been finalized.
     */
    private void handleNotifyWinLoss() {
        if (eventId == null) return;

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) return;

                    if ("finalized".equals(event.getStatus())) {
                        Toast.makeText(
                                this,
                                "Event is already finalized. Win/Loss notifications have already been sent.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (event.getWaitingList() != null) {
                        finalizeAndNotify(event);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch event data", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the final result for every participant and sends Win or Loss notifications.
     * Finalizes the event status to "finalized".
     *
     * @param event The event model to finalize.
     */
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

        // Fetch entrant profiles to check notification preferences
        db.collection("entrants")
                .whereIn("deviceId", deviceIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Boolean> notificationPrefs = new HashMap<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            notificationPrefs.put(
                                    entrant.getDeviceId(),
                                    entrant.isNotificationsEnabled()
                            );
                        }
                    }

                    WriteBatch batch = db.batch();
                    int winCount = 0;
                    int lossCount = 0;

                    for (Event.WaitingListEntry entry : waitingList) {
                        String status = entry.getParticipationStatus() != null
                                ? entry.getParticipationStatus().toLowerCase()
                                : "";

                        boolean isWinner = status.equals("selected") || status.equals("accepted");
                        boolean canNotify = notificationPrefs.getOrDefault(entry.getDeviceId(), true);

                        if (isWinner) {
                            entry.setFinalResult("WIN");
                            if (canNotify) {
                                addNotificationToBatch(
                                        batch,
                                        entry.getDeviceId(),
                                        "LOTTERY_WIN",
                                        "Congratulations! You Won",
                                        "You have been selected as a final attendee for: " + event.getTitle(),
                                        now
                                );
                            }
                            winCount++;
                        } else {
                            entry.setFinalResult("LOSS");
                            if (canNotify) {
                                addNotificationToBatch(
                                        batch,
                                        entry.getDeviceId(),
                                        "LOTTERY_LOSE",
                                        "Lottery Result Update",
                                        "We regret to inform you that you were not selected for the final attendee list for: " + event.getTitle(),
                                        now
                                );
                            }
                            lossCount++;
                        }

                        entry.setUpdatedAt(now);
                    }

                    // Update event document status and entire waiting list sub-field
                    DocumentReference eventRef = db.collection("events").document(eventId);
                    batch.update(
                            eventRef,
                            "status", "finalized",
                            "waitingList", waitingList,
                            "updatedAt", now
                    );

                    final int finalWinCount = winCount;
                    final int finalLossCount = lossCount;

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(
                                        this,
                                        String.format(Locale.getDefault(), "Finalized! Winners: %d, Losers: %d", finalWinCount, finalLossCount),
                                        Toast.LENGTH_LONG
                                ).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Finalization failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show());
                });
    }

    /**
     * Helper to add a new notification document to a Firestore WriteBatch.
     *
     * @param batch       The active batch.
     * @param recipientId The target user's device ID.
     * @param type        The notification type enum string.
     * @param title       The subject of the notification.
     * @param message     The body text of the notification.
     * @param now         The creation timestamp.
     */
    private void addNotificationToBatch(
            WriteBatch batch,
            String recipientId,
            String type,
            String title,
            String message,
            Timestamp now
    ) {
        DocumentReference notifRef = db.collection("notifications").document();

        Map<String, Object> notification = new HashMap<>();
        notification.put("notificationId", notifRef.getId());
        notification.put("recipientId", recipientId);
        notification.put("senderId", deviceId);
        notification.put("recipientType", "ENTRANT");
        notification.put("eventId", eventId);
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("isRead", false);
        notification.put("createdAt", now);
        notification.put("actionStatus", "NONE");

        batch.set(notifRef, notification);
    }

    /**
     * Filters recipients based on the currently selected tab and triggers a batch send.
     *
     * @param title The message title.
     * @param body  The message body.
     */
    private void sendNotificationToGroup(String title, String body) {
        if (eventId == null) return;

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null || event.getWaitingList() == null) return;

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
                });
    }

    /**
     * Checks if a participation status matches the requirements of the currently selected tab.
     */
    private boolean isTargetStatus(String status) {
        if (status == null) status = "waiting";
        String s = status.toLowerCase();

        switch (selectedTab) {
            case "waiting":
                return true;
            case "selected":
                return s.equals("selected") || s.equals("accepted");
            case "cancelled":
                return s.equals("cancelled") || s.equals("declined");
            default:
                return false;
        }
    }

    /**
     * Sends the notification to all valid recipients in the list while strictly 
     * respecting their individual notification permission settings.
     *
     * @param recipients List of waiting list entries to notify.
     * @param title      The message title.
     * @param body       The message body.
     */
    private void performBatchSend(List<Event.WaitingListEntry> recipients, String title, String body) {
        List<String> deviceIds = new ArrayList<>();
        for (Event.WaitingListEntry r : recipients) {
            deviceIds.add(r.getDeviceId());
        }

        db.collection("entrants")
                .whereIn("deviceId", deviceIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Boolean> notificationPrefs = new HashMap<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            notificationPrefs.put(
                                    entrant.getDeviceId(),
                                    entrant.isNotificationsEnabled()
                            );
                        }
                    }

                    WriteBatch batch = db.batch();
                    Timestamp now = Timestamp.now();
                    int sentCount = 0;

                    String type;
                    switch (selectedTab) {
                        case "waiting":
                            type = "WAITLIST_MESSAGE";
                            break;
                        case "selected":
                            type = "SELECTED_MESSAGE";
                            break;
                        case "cancelled":
                            type = "CANCELLED_MESSAGE";
                            break;
                        default:
                            type = "GENERIC_ANNOUNCEMENT";
                            break;
                    }

                    for (Event.WaitingListEntry recipient : recipients) {
                        boolean canNotify = notificationPrefs.getOrDefault(recipient.getDeviceId(), true);
                        if (canNotify) {
                            addNotificationToBatch(
                                    batch,
                                    recipient.getDeviceId(),
                                    type,
                                    title,
                                    body,
                                    now
                            );
                            sentCount++;
                        }
                    }

                    if (sentCount == 0) {
                        Toast.makeText(
                                this,
                                "No notifications sent (recipients have disabled notifications).",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    final int finalSentCount = sentCount;
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(
                                        this,
                                        "Notifications sent to " + finalSentCount + " recipients",
                                        Toast.LENGTH_SHORT
                                ).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to send notifications",
                                            Toast.LENGTH_SHORT
                                    ).show());
                });
    }
}
