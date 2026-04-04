package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.lottery_legend.event.EventDetailsActivity;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
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
        if (deviceId == null) return;

        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
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
                            "You have %d unread notifications",
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

    /**
     * These types can be overridden by current selection state.
     */
    private boolean shouldUseSelectionStateOverride(Notification notification) {
        if (notification == null || notification.getType() == null) return false;

        String type = notification.getType();
        return "LOTTERY_WIN".equals(type)
                || "SELECTED_MESSAGE".equals(type)
                || "PRIVATE_EVENT_INVITE".equals(type);
    }

    private void handleNotificationClick(Notification notification) {
        if (notification == null) return;

        markSingleNotificationAsRead(notification);

        if (notification.getEventId() != null && shouldUseSelectionStateOverride(notification)) {
            findParticipationStatus(notification.getEventId(), participationStatus -> {
                if ("cancelled".equalsIgnoreCase(participationStatus)) {
                    showSelectionCancelDialog(notification);
                    return;
                }

                if ("declined".equalsIgnoreCase(participationStatus)) {
                    showDeclinedDialog(notification);
                    return;
                }

                findLatestCancelledMessageForEvent(notification.getEventId(), cancelledNotification -> {
                    if (cancelledNotification != null) {
                        showSelectionCancelDialog(cancelledNotification);
                    } else {
                        processNotificationByType(notification);
                    }
                });
            });
        } else {
            processNotificationByType(notification);
        }
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

    /**
     * Find latest CANCELLED_MESSAGE for the same recipient + event.
     */
    private void findLatestCancelledMessageForEvent(String eventId, OnCancelledNotificationFound callback) {
        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "CANCELLED_MESSAGE")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Notification cancelled = extractFirstNotification(queryDocumentSnapshots);
                    callback.onFound(cancelled);
                })
                .addOnFailureListener(e -> callback.onFound(null));
    }

    /**
     * Find current participationStatus from event.waitingList for this deviceId.
     */
    private void findParticipationStatus(String eventId, OnParticipationStatusFound callback) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onFound(null);
                        return;
                    }

                    Event event = doc.toObject(Event.class);
                    if (event == null || event.getWaitingList() == null) {
                        callback.onFound(null);
                        return;
                    }

                    for (Event.WaitingListEntry entry : event.getWaitingList()) {
                        if (entry != null && deviceId != null && deviceId.equals(entry.getDeviceId())) {
                            callback.onFound(entry.getParticipationStatus());
                            return;
                        }
                    }

                    callback.onFound(null);
                })
                .addOnFailureListener(e -> callback.onFound(null));
    }

    private Notification extractFirstNotification(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) return null;

        DocumentSnapshot doc = snapshots.getDocuments().get(0);
        Notification notification = doc.toObject(Notification.class);
        if (notification != null) {
            notification.setNotificationId(doc.getId());
        }
        return notification;
    }

    private void processNotificationByType(Notification notification) {
        String type = notification.getType() != null ? notification.getType() : "";

        switch (type) {
            case "LOTTERY_WIN":
            case "PRIVATE_EVENT_INVITE":
            case "SELECTED_MESSAGE":
                showInvitationDialog(notification);
                break;

            case "CANCELLED_MESSAGE":
                showSelectionCancelDialog(notification);
                break;

            case "LOTTERY_LOSE":
            case "WAITLIST_MESSAGE":
            case "CO_ORGANIZER_INVITE":
            case "GENERAL":
            default:
                if (notification.getEventId() != null) {
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("eventId", notification.getEventId());
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                }
                break;
        }
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
        TextView tvDate = dialogView.findViewById(R.id.tvEventDate);
        TextView tvRespondBy = dialogView.findViewById(R.id.tvRespondBy);
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
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

                        if (event.getEventStartAt() != null) {
                            tvDate.setText(String.format(
                                    Locale.getDefault(),
                                    "Event: %s",
                                    sdf.format(event.getEventStartAt().toDate())
                            ));
                        } else {
                            tvDate.setText("Event date unavailable");
                        }

                        tvRespondBy.setText("Action required promptly");
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
            acceptInvitation(notification);
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
            declineInvitation(notification);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSelectionCancelDialog(Notification notification) {
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

        tvTitle.setText("Selection Cancelled");

        if (notification != null
                && notification.getMessage() != null
                && !notification.getMessage().trim().isEmpty()) {
            tvMessage.setText(notification.getMessage());
        } else {
            tvMessage.setText("Your previous selection for this event is no longer valid.");
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeclinedDialog(Notification notification) {
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

        tvTitle.setText("Invitation Declined");
        tvMessage.setText("You have already declined this invitation.");

        btnClose.setOnClickListener(v -> dialog.dismiss());

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

    private void declineInvitation(Notification notification) {
        if (notification == null || notification.getEventId() == null) return;

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;

            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId != null && deviceId.equals(entry.getDeviceId())) {
                    entry.setParticipationStatus("declined");
                    updated = true;
                    break;
                }
            }

            if (!updated) return;

            db.collection("events").document(notification.getEventId())
                    .update("waitingList", list)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                        db.collection("notifications")
                                .document(notification.getNotificationId())
                                .update("actionStatus", "DECLINED");
                    });
        });
    }

    private interface OnCancelledNotificationFound {
        void onFound(Notification cancelledNotification);
    }

    private interface OnParticipationStatusFound {
        void onFound(String participationStatus);
    }
}