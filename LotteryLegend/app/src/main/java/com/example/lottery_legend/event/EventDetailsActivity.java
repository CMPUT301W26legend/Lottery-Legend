package com.example.lottery_legend.event;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.entrant.CommentsActivity;
import com.example.lottery_legend.entrant.NavbarEntrant;
import com.example.lottery_legend.entrant.ProfileActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity that displays the detailed information of a specific event.
 */
public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    private ImageView posterImage;
    private ImageView imageOrganizerAvatar;
    private TextView textEventTitle;
    private TextView textRegistrationStatus;
    private TextView textEventDate;
    private TextView textRegistrationDeadline;
    private TextView textLocation;
    private TextView textPrice;
    private TextView textCapacity;
    private TextView textWaitingList;
    private TextView textAboutEvent;
    private TextView textLotteryGuidelines;
    private TextView textOrganizerName;
    private TextView tagPrivate;
    private LinearLayout layoutOrganizerProfile;
    private LinearLayout layoutLotteryResponse;
    private MaterialButton btnJoinWaitingList, btnAcceptInvitation, btnDeclineInvitation;
    private ImageButton shareIcon;
    private ImageButton commentIcon;
    private MaterialToolbar toolbar;
    private FrameLayout layoutNotification;
    private TextView tvNotificationBadge;

    private String organizerId;
    private String currentUserName;

    private ListenerRegistration notificationBadgeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        deviceId = getIntent().getStringExtra("deviceId");

        setupViews();
        fetchEventDetails();
        fetchCurrentUserName();

        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.HOME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationBadgeListener != null) {
            notificationBadgeListener.remove();
            notificationBadgeListener = null;
        }
    }

    private void setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbarEventDetails);
        toolbar.setNavigationOnClickListener(v -> finish());

        posterImage = findViewById(R.id.posterImage);
        imageOrganizerAvatar = findViewById(R.id.imageOrganizerAvatar);
        textEventTitle = findViewById(R.id.textEventTitle);
        textRegistrationStatus = findViewById(R.id.textRegistrationStatus);
        textEventDate = findViewById(R.id.textEventDate);
        textRegistrationDeadline = findViewById(R.id.textRegistrationDeadline);
        textLocation = findViewById(R.id.textLocation);
        textPrice = findViewById(R.id.textPrice);
        textCapacity = findViewById(R.id.textCapacity);
        textWaitingList = findViewById(R.id.textWaitingList);
        textAboutEvent = findViewById(R.id.textAboutEvent);
        textLotteryGuidelines = findViewById(R.id.textLotteryGuidelines);
        textOrganizerName = findViewById(R.id.textOrganizerName);
        tagPrivate = findViewById(R.id.tagPrivate);
        layoutOrganizerProfile = findViewById(R.id.layoutOrganizerProfile);
        layoutLotteryResponse = findViewById(R.id.layoutLotteryResponse);
        btnJoinWaitingList = findViewById(R.id.btnJoinWaitingList);
        btnAcceptInvitation = findViewById(R.id.btnAcceptInvitation);
        btnDeclineInvitation = findViewById(R.id.btnDeclineInvitation);
        shareIcon = findViewById(R.id.shareIcon);
        commentIcon = findViewById(R.id.commentIcon);
        layoutNotification = findViewById(R.id.layoutNotification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

        layoutOrganizerProfile.setOnClickListener(v -> {
            if (organizerId != null) {
                Intent intent = new Intent(EventDetailsActivity.this, ProfileActivity.class);
                intent.putExtra("deviceId", organizerId);
                intent.putExtra("isReadOnly", true);
                intent.putExtra("isOrganizerMode", true);
                startActivity(intent);
            }
        });

        shareIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, ShareQRCodeActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });

        commentIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, CommentsActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("deviceId", deviceId);
            intent.putExtra("authorName", currentUserName != null ? currentUserName : "Anonymous");
            intent.putExtra("authorType", "ENTRANT");
            startActivity(intent);
        });

        layoutNotification.setOnClickListener(v -> {
            if (deviceId == null || eventId == null || organizerId == null) {
                Toast.makeText(this, "Notification data not ready", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(EventDetailsActivity.this, EventNotificationActivity.class);
            intent.putExtra("deviceId", deviceId);
            intent.putExtra("eventId", eventId);
            intent.putExtra("organizerId", organizerId);
            startActivity(intent);
        });

        btnAcceptInvitation.setOnClickListener(v -> handleAcceptInvitation());
        btnDeclineInvitation.setOnClickListener(v -> showDeclineConfirmationDialog());
    }

    private void setupNotificationBadge() {
        if (deviceId == null || eventId == null || organizerId == null) {
            tvNotificationBadge.setVisibility(View.GONE);
            return;
        }

        if (notificationBadgeListener != null) {
            notificationBadgeListener.remove();
            notificationBadgeListener = null;
        }

        notificationBadgeListener = db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("recipientType", "ENTRANT")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("senderId", organizerId)
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

    private void fetchCurrentUserName() {
        if (deviceId == null) return;

        db.collection("entrants").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUserName = doc.getString("name");
            }
        });
    }

    private void fetchEventDetails() {
        if (eventId == null) return;

        db.collection("events").document(eventId).addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null) {
                    populateViews(event);
                }
            }
        });
    }

    private void populateViews(Event event) {
        textEventTitle.setText(event.getTitle());
        textAboutEvent.setText(event.getDescription());
        organizerId = event.getOrganizerId();

        fetchOrganizerData(organizerId);
        setupNotificationBadge();

        Event.EventLocation loc = event.getEventLocation();
        textLocation.setText(loc != null ? loc.getName() : "No location provided");

        textPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
        textCapacity.setText(event.getCapacity() + " Spots");

        int waitingListSize = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        if (event.getMaxWaitingList() != null) {
            textWaitingList.setText(String.format(
                    Locale.getDefault(),
                    "%d/%d entrants registered",
                    waitingListSize,
                    event.getMaxWaitingList()
            ));
        } else {
            textWaitingList.setText(String.format(
                    Locale.getDefault(),
                    "%d entrants registered",
                    waitingListSize
            ));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());

        if (event.getEventStartAt() != null) {
            textEventDate.setText(sdf.format(event.getEventStartAt().toDate()));
        } else {
            textEventDate.setText("No event date provided");
        }

        if (event.getRegistrationEndAt() != null) {
            textRegistrationDeadline.setText(sdf.format(event.getRegistrationEndAt().toDate()));
        } else {
            textRegistrationDeadline.setText("No registration deadline");
        }

        if (event.getDrawAt() != null) {
            String drawDateStr = sdf.format(event.getDrawAt().toDate());
            String guidelines = "• Random selection on " + drawDateStr + "\n"
                    + "• Winners notified via app\n"
                    + "• 48 hours to accept invitation\n"
                    + "• Replacements drawn if declined";
            textLotteryGuidelines.setText(guidelines);
        } else {
            textLotteryGuidelines.setText("No lottery guidelines available");
        }

        boolean isJoined = false;
        String participationStatus = null;
        if (event.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : event.getWaitingList()) {
                if (Objects.equals(entry.getDeviceId(), deviceId)) {
                    isJoined = true;
                    participationStatus = entry.getParticipationStatus();
                    break;
                }
            }
        }

        updateStatusUI(event, isJoined, participationStatus);

        if (event.isIsPrivateEvent()) {
            tagPrivate.setVisibility(View.VISIBLE);
        } else {
            tagPrivate.setVisibility(View.GONE);
        }

        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    posterImage.setImageBitmap(bitmap);
                } else {
                    posterImage.setImageResource(R.drawable.img_poster);
                }
            } catch (Exception e) {
                posterImage.setImageResource(R.drawable.img_poster);
            }
        } else {
            posterImage.setImageResource(R.drawable.img_poster);
        }
    }

    private void updateStatusUI(Event event, boolean isJoined, String participationStatus) {
        Timestamp now = Timestamp.now();
        String eventStatus = event.getStatus() != null ? event.getStatus().toLowerCase() : "open";

        boolean isPastStartDate = event.getEventStartAt() != null
                && event.getEventStartAt().compareTo(now) < 0;

        // Reset visibility
        layoutLotteryResponse.setVisibility(View.GONE);
        btnJoinWaitingList.setVisibility(View.GONE);

        if (isJoined && ("selected".equalsIgnoreCase(participationStatus) || "invited".equalsIgnoreCase(participationStatus))) {
            textRegistrationStatus.setText("Selected!");
            textRegistrationStatus.setTextColor(Color.parseColor("#2563EB"));
            layoutLotteryResponse.setVisibility(View.VISIBLE);
            return;
        }

        if (isJoined && "accepted".equalsIgnoreCase(participationStatus)) {
            textRegistrationStatus.setText("Accepted");
            textRegistrationStatus.setTextColor(Color.parseColor("#388E3C"));
            return;
        }

        if (isJoined && ("declined".equalsIgnoreCase(participationStatus) || "cancelled".equalsIgnoreCase(participationStatus))) {
            textRegistrationStatus.setText("Cancelled/Declined");
            textRegistrationStatus.setTextColor(Color.parseColor("#EF4444"));
            return;
        }

        if (isPastStartDate || "closed".equals(eventStatus)) {
            textRegistrationStatus.setText("Closed");
            textRegistrationStatus.setTextColor(Color.parseColor("#9CA3AF"));

            if (isJoined) {
                btnJoinWaitingList.setVisibility(View.VISIBLE);
                btnJoinWaitingList.setText("Leave Waiting List");
                btnJoinWaitingList.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#EF4444"))
                );
                btnJoinWaitingList.setOnClickListener(v ->
                        WaitingListDialogFragment.newInstance(event, deviceId)
                                .show(getSupportFragmentManager(), "Leave Waiting List")
                );
            }

            if (isPastStartDate && !"closed".equals(eventStatus) && event.getEventId() != null) {
                db.collection("events").document(event.getEventId()).update("status", "closed");
            }

        } else if ("drawed".equals(eventStatus) || "drawn".equals(eventStatus)) {
            textRegistrationStatus.setText("Drawn");
            textRegistrationStatus.setTextColor(Color.parseColor("#F57C00"));
            if (isJoined) {
                btnJoinWaitingList.setVisibility(View.VISIBLE);
                btnJoinWaitingList.setText("Leave Waiting List");
                btnJoinWaitingList.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#EF4444"))
                );
                btnJoinWaitingList.setOnClickListener(v ->
                        WaitingListDialogFragment.newInstance(event, deviceId)
                                .show(getSupportFragmentManager(), "Leave Waiting List")
                );
            }

        } else {
            btnJoinWaitingList.setVisibility(View.VISIBLE);

            if (isJoined) {
                textRegistrationStatus.setText("Joined");
                textRegistrationStatus.setTextColor(Color.parseColor("#F59E0B"));
                btnJoinWaitingList.setText("Leave Waiting List");
                btnJoinWaitingList.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#EF4444"))
                );
                btnJoinWaitingList.setOnClickListener(v ->
                        WaitingListDialogFragment.newInstance(event, deviceId)
                                .show(getSupportFragmentManager(), "Leave Waiting List")
                );
            } else {
                textRegistrationStatus.setText("Active");
                textRegistrationStatus.setTextColor(Color.parseColor("#388E3C"));
                btnJoinWaitingList.setText("Join Waiting List");
                btnJoinWaitingList.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                );
                btnJoinWaitingList.setOnClickListener(v ->
                        WaitingListDialogFragment.newInstance(event, deviceId)
                                .show(getSupportFragmentManager(), "Join Waiting List")
                );
            }
        }
    }

    private void handleAcceptInvitation() {
        if (eventId == null || deviceId == null) return;

        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;

            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId.equals(entry.getDeviceId())) {
                    entry.setParticipationStatus("accepted");
                    entry.setRespondedAt(Timestamp.now());
                    entry.setUpdatedAt(Timestamp.now());
                    updated = true;
                    break;
                }
            }

            if (updated) {
                db.collection("events").document(eventId)
                        .update("waitingList", list)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Invitation accepted", Toast.LENGTH_SHORT).show();
                            updateNotificationStatus("ACCEPTED");
                        });
            }
        });
    }

    private void showDeclineConfirmationDialog() {
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
            handleDeclineInvitation();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void handleDeclineInvitation() {
        if (eventId == null || deviceId == null) return;

        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;
            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId.equals(entry.getDeviceId())) {
                    entry.setParticipationStatus("declined");
                    entry.setDeclinedAt(Timestamp.now());
                    entry.setUpdatedAt(Timestamp.now());
                    updated = true;
                    break;
                }
            }

            if (updated) {
                db.collection("events").document(eventId)
                        .update("waitingList", list)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                            updateNotificationStatus("DECLINED");
                        });
            }
        });
    }

    private void updateNotificationStatus(String status) {
        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("eventId", eventId)
                .whereIn("type", List.of("LOTTERY_WIN", "SELECTED_MESSAGE", "PRIVATE_INVITE", "PRIVATE_EVENT_INVITE"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        db.collection("notifications").document(doc.getId())
                                .update("actionStatus", status, "isRead", true);
                    }
                });
    }

    private void fetchOrganizerData(String id) {
        if (id == null) return;

        db.collection("organizers").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String organizerName = doc.getString("name");
                textOrganizerName.setText(
                        organizerName != null && !organizerName.trim().isEmpty()
                                ? organizerName
                                : "Organizer"
                );
                
                String profileImage = doc.getString("profileImage");
                if (profileImage != null && !profileImage.isEmpty()) {
                    displayBase64Image(profileImage, imageOrganizerAvatar);
                } else {
                    imageOrganizerAvatar.setImageResource(R.drawable.ic_profile_avatar);
                }
            } else {
                textOrganizerName.setText("Organizer");
                imageOrganizerAvatar.setImageResource(R.drawable.ic_profile_avatar);
            }
        }).addOnFailureListener(e -> {
            textOrganizerName.setText("Organizer");
            imageOrganizerAvatar.setImageResource(R.drawable.ic_profile_avatar);
        });
    }

    private void displayBase64Image(String base64, ImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception ignored) {
        }
    }
}
