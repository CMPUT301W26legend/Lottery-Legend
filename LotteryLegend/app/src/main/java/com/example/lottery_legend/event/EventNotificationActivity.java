package com.example.lottery_legend.event;

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
import com.example.lottery_legend.entrant.NotificationAdapter;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                        Toast.makeText(this, "Failed to load event notifications", Toast.LENGTH_SHORT).show();
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

        if (!notification.getIsRead()) {
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

        processNotificationByType(notification);
    }

    private void processNotificationByType(Notification notification) {
        String type = notification.getType() != null ? notification.getType() : "";

        switch (type) {
            case "LOTTERY_WIN":
            case "PRIVATE_EVENT_INVITE":
            case "SELECTED_MESSAGE":
                showSimpleMessageDialog(notification, "Invitation");
                break;

            case "CANCELLED_MESSAGE":
                showSimpleMessageDialog(notification, "Selection Cancelled");
                break;

            case "LOTTERY_LOSE":
            case "WAITLIST_MESSAGE":
            case "CO_ORGANIZER_INVITE":
            case "GENERAL":
            default:
                showSimpleMessageDialog(notification,
                        notification.getTitle() != null ? notification.getTitle() : "Notification");
                break;
        }
    }

    private void showSimpleMessageDialog(Notification notification, String defaultTitle) {
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

        tvTitle.setText(notification.getTitle() != null && !notification.getTitle().trim().isEmpty()
                ? notification.getTitle()
                : defaultTitle);

        tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}