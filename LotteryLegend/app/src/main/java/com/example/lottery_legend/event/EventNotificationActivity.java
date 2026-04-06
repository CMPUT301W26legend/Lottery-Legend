package com.example.lottery_legend.event;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.entrant.EntrantActionHelper;
import com.example.lottery_legend.entrant.NavbarEntrant;
import com.example.lottery_legend.entrant.NotificationAdapter;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventNotificationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private String eventId;
    private String organizerId;

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private final List<Notification> allNotifications = new ArrayList<>();
    private final List<Notification> filteredNotifications = new ArrayList<>();

    private TextView tvUnreadSummary, tvMarkAll;
    private TextView tabAll, tabUnread, tabRead;

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");
        eventId = getIntent().getStringExtra("eventId");
        organizerId = getIntent().getStringExtra("organizerId");

        setupViews();
        setupListeners();
        fetchNotifications();

        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.HOME);
    }

    private void setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarNotification);
        toolbar.setTitle("Event Notifications");
        toolbar.setNavigationOnClickListener(v -> finish());

        tvUnreadSummary = findViewById(R.id.tvUnreadSummary);
        tvMarkAll = findViewById(R.id.tvMarkAll);
        tabAll = findViewById(R.id.tabAll);
        tabUnread = findViewById(R.id.tabUnread);
        tabRead = findViewById(R.id.tabRead);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(filteredNotifications, this::handleNotificationClick);
        rvNotifications.setAdapter(adapter);

        updateTabsUI();
    }

    private void setupListeners() {
        tvMarkAll.setOnClickListener(v -> markAllAsRead());

        tabAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateTabsUI();
        });

        tabUnread.setOnClickListener(v -> {
            currentFilter = "UNREAD";
            updateTabsUI();
        });

        tabRead.setOnClickListener(v -> {
            currentFilter = "READ";
            updateTabsUI();
        });
    }

    private void fetchNotifications() {
        if (deviceId == null || eventId == null || organizerId == null) return;

        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("recipientType", "ENTRANT")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("senderId", organizerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("EventNotificationAct", "Error loading notifications", error);
                        return;
                    }

                    allNotifications.clear();
                    int unreadCount = 0;

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notification.setNotificationId(doc.getId());
                                allNotifications.add(notification);
                                if (!notification.getIsRead()) {
                                    unreadCount++;
                                }
                            }
                        }
                    }

                    tvUnreadSummary.setText(String.format(
                            Locale.getDefault(),
                            "You have %d unread event notifications",
                            unreadCount
                    ));

                    applyFilter();
                });
    }

    private void applyFilter() {
        filteredNotifications.clear();

        for (Notification n : allNotifications) {
            if ("ALL".equals(currentFilter)) {
                filteredNotifications.add(n);
            } else if ("UNREAD".equals(currentFilter) && !n.getIsRead()) {
                filteredNotifications.add(n);
            } else if ("READ".equals(currentFilter) && n.getIsRead()) {
                filteredNotifications.add(n);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void updateTabsUI() {
        resetTabs();

        if ("ALL".equals(currentFilter)) {
            setSelectedTab(tabAll);
        } else if ("UNREAD".equals(currentFilter)) {
            setSelectedTab(tabUnread);
        } else if ("READ".equals(currentFilter)) {
            setSelectedTab(tabRead);
        }

        applyFilter();
    }

    private void resetTabs() {
        tabAll.setBackgroundResource(R.drawable.bg_notification_tab_unselected);
        tabAll.setTextColor(ContextCompat.getColor(this, R.color.gray_tab_text));

        tabUnread.setBackgroundResource(R.drawable.bg_notification_tab_unselected);
        tabUnread.setTextColor(ContextCompat.getColor(this, R.color.gray_tab_text));

        tabRead.setBackgroundResource(R.drawable.bg_notification_tab_unselected);
        tabRead.setTextColor(ContextCompat.getColor(this, R.color.gray_tab_text));
    }

    private void setSelectedTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_notification_tab_selected);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void markAllAsRead() {
        WriteBatch batch = db.batch();
        boolean hasUnread = false;

        for (Notification n : allNotifications) {
            if (!n.getIsRead()) {
                batch.update(
                        db.collection("notifications").document(n.getNotificationId()),
                        "isRead",
                        true
                );
                hasUnread = true;
            }
        }

        if (hasUnread) {
            batch.commit().addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "All marked as read", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void handleNotificationClick(Notification notification) {
        if (notification == null) return;

        markSingleNotificationAsRead(notification);

        String type = notification.getType() != null ? notification.getType() : "";
        String actionStatus = notification.getActionStatus() != null 
                ? notification.getActionStatus().toUpperCase(Locale.ROOT) 
                : "PENDING";

        switch (type) {
            case "LOTTERY_WIN":
            case "LOTTERY_LOSE":
            case "WAITLIST_MESSAGE":
            case "CANCELLED_MESSAGE":
            case "GENERAL":
            case "GENERIC_ANNOUNCEMENT":
                showSimpleMessageDialog(notification);
                break;

            case "PRIVATE_EVENT_INVITE":
            case "PRIVATE_INVITE":
                handleActionableType(notification, actionStatus, "PRIVATE");
                break;

            case "SELECTED_MESSAGE":
            case "SIGN_UP_MESSAGE":
                handleActionableType(notification, actionStatus, "INVITATION");
                break;

            case "CO_ORGANIZER_INVITE":
                handleActionableType(notification, actionStatus, "CO_ORGANIZER");
                break;

            default:
                showSimpleMessageDialog(notification);
                break;
        }
    }

    private void handleActionableType(Notification notification, String actionStatus, String dialogType) {
        if ("ACCEPTED".equals(actionStatus)) {
            showStatusDialog(notification, "Already Accepted", "You have already accepted this invitation.");
        } else if ("DECLINED".equals(actionStatus)) {
            showStatusDialog(notification, "Already Declined", "You have already declined this invitation.");
        } else {
            switch (dialogType) {
                case "PRIVATE":
                    showPrivateInvitationDialog(notification);
                    break;
                case "INVITATION":
                    showInvitationDialog(notification);
                    break;
                case "CO_ORGANIZER":
                    showCoOrganizerInvitationDialog(notification);
                    break;
            }
        }
    }

    private void showStatusDialog(Notification notification, String title, String message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_cancel, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvCancelTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvCancelMessage);
        Button btnClose = dialogView.findViewById(R.id.btnCancelDismiss);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSimpleMessageDialog(Notification notification) {
        if (notification == null) return;
        showStatusDialog(notification, 
                notification.getTitle() != null ? notification.getTitle() : "Notification",
                notification.getMessage() != null ? notification.getMessage() : "");
    }

    private void markSingleNotificationAsRead(Notification notification) {
        if (notification.getIsRead()) return;

        notification.setIsRead(true);

        int position = filteredNotifications.indexOf(notification);
        if (position != -1) {
            adapter.notifyItemChanged(position);
        } else {
            adapter.notifyDataSetChanged();
        }

        db.collection("notifications")
                .document(notification.getNotificationId())
                .update("isRead", true);
    }

    private void showInvitationDialog(Notification notification) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_invite, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvInviteEventName);
        TextView tvMessage = dialogView.findViewById(R.id.tvInviteMessage);
        ImageView ivClose = dialogView.findViewById(R.id.ivCloseInviteDialog);
        Button btnDecline = dialogView.findViewById(R.id.btnDeclineInvite);
        Button btnAccept = dialogView.findViewById(R.id.btnAcceptInvite);

        tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");

        if (notification.getEventId() != null) {
            db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        tvTitle.setText(event.getTitle() != null ? event.getTitle() : "Event");
                    }
                }
            });
        }

        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnDecline.setOnClickListener(v -> {
            dialog.dismiss();
            showDeclineDialog(notification);
        });

        btnAccept.setOnClickListener(v -> {
            if ("SIGN_UP_MESSAGE".equals(notification.getType())) {
                EntrantActionHelper.acceptSignUp(this, deviceId, notification, null);
            } else {
                acceptInvitation(notification);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showPrivateInvitationDialog(Notification notification) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_private_event_invite, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvEventName = dialogView.findViewById(R.id.tvEventName);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        ImageView ivClose = dialogView.findViewById(R.id.btnClose);
        Button btnDecline = dialogView.findViewById(R.id.btnDecline);
        Button btnAccept = dialogView.findViewById(R.id.btnAccept);

        tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");

        if (notification.getEventId() != null) {
            db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        tvEventName.setText(event.getTitle() != null ? event.getTitle() : "Private Event");
                    }
                }
            });
        }

        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnDecline.setOnClickListener(v -> {
            declineInvitation(notification);
            dialog.dismiss();
        });

        btnAccept.setOnClickListener(v -> {
            acceptInvitation(notification);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showCoOrganizerInvitationDialog(Notification notification) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_co_organizer_invitation, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvEventName = dialogView.findViewById(R.id.tvEventName);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        ImageView ivClose = dialogView.findViewById(R.id.btnClose);
        Button btnDecline = dialogView.findViewById(R.id.btnDecline);
        Button btnAccept = dialogView.findViewById(R.id.btnAccept);

        tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");

        if (notification.getEventId() != null) {
            db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        tvEventName.setText(event.getTitle() != null ? event.getTitle() : "Event");
                    }
                }
            });
        }

        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnDecline.setOnClickListener(v -> {
            declineCoOrganizerInvitation(notification);
            dialog.dismiss();
        });

        btnAccept.setOnClickListener(v -> {
            acceptCoOrganizerInvitation(notification);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeclineDialog(Notification notification) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_decline, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancelDecline);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmDecline);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            if ("SIGN_UP_MESSAGE".equals(notification.getType())) {
                EntrantActionHelper.declineSignUp(this, deviceId, notification, null);
            } else {
                declineInvitation(notification);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void acceptInvitation(Notification notification) {
        if (notification == null || notification.getEventId() == null) return;

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;

            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId != null && deviceId.equals(entry.getDeviceId())) {
                    entry.setParticipationStatus("accepted");
                    entry.setRespondedAt(Timestamp.now());
                    entry.setUpdatedAt(Timestamp.now());
                    updated = true;
                    break;
                }
            }

            if (!updated) return;

            db.collection("events").document(notification.getEventId())
                    .update("waitingList", list)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Invitation accepted", Toast.LENGTH_SHORT).show();
                        db.collection("notifications")
                                .document(notification.getNotificationId())
                                .update("actionStatus", "ACCEPTED");
                    });
        });
    }

    private void acceptCoOrganizerInvitation(Notification notification) {
        if (notification == null || notification.getEventId() == null) return;

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null) return;

            WriteBatch batch = db.batch();

            if (event.getWaitingList() != null) {
                List<Event.WaitingListEntry> list = event.getWaitingList();
                Event.WaitingListEntry toRemove = null;
                for (Event.WaitingListEntry entry : list) {
                    if (entry != null && deviceId != null && deviceId.equals(entry.getDeviceId())) {
                        toRemove = entry;
                        break;
                    }
                }
                if (toRemove != null) {
                    list.remove(toRemove);
                    batch.update(db.collection("events").document(notification.getEventId()), "waitingList", list);
                }
            }

            Map<String, Object> coOrganizerData = new HashMap<>();
            coOrganizerData.put("deviceId", deviceId);
            coOrganizerData.put("organizerId", notification.getSenderId());
            coOrganizerData.put("status", "ACCEPTED");
            coOrganizerData.put("invitedAt", notification.getCreatedAt());
            coOrganizerData.put("acceptedAt", Timestamp.now());
            coOrganizerData.put("respondedAt", Timestamp.now());

            batch.set(db.collection("events").document(notification.getEventId())
                    .collection("coOrganizers").document(deviceId), coOrganizerData);

            batch.update(db.collection("notifications").document(notification.getNotificationId()), "actionStatus", "ACCEPTED");

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "You are now a co-organizer!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to accept invitation", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void declineCoOrganizerInvitation(Notification notification) {
        if (notification == null) return;

        db.collection("notifications")
                .document(notification.getNotificationId())
                .update("actionStatus", "DECLINED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                });
    }

    private void declineInvitation(Notification notification) {
        if (notification == null || notification.getEventId() == null) return;

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;
            Event.WaitingListEntry toRemove = null;

            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId != null && deviceId.equals(entry.getDeviceId())) {
                    if ("SIGN_UP_MESSAGE".equals(notification.getType())) {
                        entry.setParticipationStatus("declined");
                        entry.setUpdatedAt(Timestamp.now());
                        entry.setDeclinedAt(Timestamp.now());
                        updated = true;
                    } else {
                        toRemove = entry;
                    }
                    break;
                }
            }

            if (updated || toRemove != null) {
                if (toRemove != null) {
                    list.remove(toRemove);
                }
                db.collection("events").document(notification.getEventId())
                        .update("waitingList", list)
                        .addOnSuccessListener(aVoid -> {
                            db.collection("notifications")
                                    .document(notification.getNotificationId())
                                    .update("actionStatus", "DECLINED");
                            Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
