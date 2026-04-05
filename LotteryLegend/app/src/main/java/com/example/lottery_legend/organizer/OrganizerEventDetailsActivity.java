package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.MapActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for organizers to view the details of a specific event they have created.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity implements PosterUploadDialogFragment.OnPosterEventListener {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    private ImageView eventPoster;
    private TextView textWaitingCount, textSelectedCount, textCapacity;
    private TextView textEventTitle, textEventStatus;
    private TextView textEventDate, textRegistrationDeadline, textLocation, textPrice;
    private TextView textDescription, textLotteryGuidelines;
    private Button btnViewWaitingList, btnRunLotteryDraw, btnSendNotification, btnDeleteEvent;
    private ImageButton editIcon, updatePoster, commentIcon, mapIcon, shareIcon;

    private String currentPosterBase64;
    private String currentUserName;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        deviceId = getIntent().getStringExtra("deviceId");

        initViews();
        setupListeners();
        fetchEventDetails();
        fetchCurrentUserName();

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HISTORY);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarOrganizerDetails);
        toolbar.setNavigationOnClickListener(v -> finish());

        editIcon = findViewById(R.id.editIcon);
        updatePoster = findViewById(R.id.updatePoster);
        commentIcon = findViewById(R.id.commentIcon);
        mapIcon = findViewById(R.id.mapicon);
        shareIcon = findViewById(R.id.shareIcon);

        eventPoster = findViewById(R.id.eventPoster);
        textWaitingCount = findViewById(R.id.textWaitingCount);
        textSelectedCount = findViewById(R.id.textSelectedCount);
        textCapacity = findViewById(R.id.textCapacity);
        textEventTitle = findViewById(R.id.textEventTitle);
        textEventStatus = findViewById(R.id.textEventStatus);
        textEventDate = findViewById(R.id.textEventDate);
        textRegistrationDeadline = findViewById(R.id.textRegistrationDeadline);
        textLocation = findViewById(R.id.textLocation);
        textPrice = findViewById(R.id.textPrice);
        textDescription = findViewById(R.id.textDescription);
        textLotteryGuidelines = findViewById(R.id.textLotteryGuidelines);

        btnViewWaitingList = findViewById(R.id.btnViewWaitingList);
        btnRunLotteryDraw = findViewById(R.id.btnRunLotteryDraw);
        btnSendNotification = findViewById(R.id.btnSendNotification);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
    }

    private void setupListeners() {
        shareIcon.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerQRCodeActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
        });

        editIcon.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, EditEventActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
        });

        updatePoster.setOnClickListener(v -> {
            PosterUploadDialogFragment dialog = new PosterUploadDialogFragment();
            if (currentPosterBase64 != null && !currentPosterBase64.isEmpty()) {
                dialog.setCurrentBase64(currentPosterBase64);
            }
            dialog.setOnPosterEventListener(this);
            dialog.show(getSupportFragmentManager(), "PosterUploadDialog");
        });

        commentIcon.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerCommentsActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("authorName", currentUserName != null ? currentUserName : "Organizer");
                startActivity(intent);
            }
        });

        mapIcon.setOnClickListener(v -> {
            if (currentEvent != null && currentEvent.getEventLocation() != null) {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra(MapActivity.EXTRA_LATITUDE, currentEvent.getEventLocation().getLatitude());
                intent.putExtra(MapActivity.EXTRA_LONGITUDE, currentEvent.getEventLocation().getLongitude());
                intent.putExtra(MapActivity.EXTRA_MARKER_NAME, currentEvent.getTitle());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Event location not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnViewWaitingList.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, WaitingListActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
        });

        btnRunLotteryDraw.setOnClickListener(v -> showRunLotteryDrawDialog());

        btnSendNotification.setOnClickListener(v ->
                Toast.makeText(this, "Send Notification coming soon", Toast.LENGTH_SHORT).show()
        );

        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void fetchCurrentUserName() {
        if (deviceId == null) return;

        db.collection("organizers").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUserName = doc.getString("name");
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        if (eventId == null) return;

        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show()
                );
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
        this.currentEvent = event;

        textEventTitle.setText(event.getTitle());
        textDescription.setText(event.getDescription());
        textCapacity.setText(String.valueOf(event.getCapacity()));

        int waitingListSize = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        if (event.getMaxWaitingList() != null) {
            textWaitingCount.setText(String.format(Locale.getDefault(), "%d/%d", waitingListSize, event.getMaxWaitingList()));
        } else {
            textWaitingCount.setText(String.valueOf(waitingListSize));
        }

        // Count already sampled entrants: selected + accepted only
        int selected = 0;
        if (event.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : event.getWaitingList()) {
                if (entry == null) continue;

                String status = entry.getParticipationStatus();
                if (status != null &&
                        ("selected".equalsIgnoreCase(status) ||
                                "accepted".equalsIgnoreCase(status))) {
                    selected++;
                }
            }
        }
        textSelectedCount.setText(String.valueOf(selected));

        Event.EventLocation loc = event.getEventLocation();
        textLocation.setText(loc != null ? loc.getName() : "No location provided");

        textPrice.setText(event.getPrice() == 0
                ? "Free Entry"
                : String.format(Locale.getDefault(), "$%.2f", event.getPrice()));

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());
        if (event.getEventStartAt() != null) {
            textEventDate.setText(sdf.format(event.getEventStartAt().toDate()));
        }
        if (event.getRegistrationEndAt() != null) {
            textRegistrationDeadline.setText(sdf.format(event.getRegistrationEndAt().toDate()));
        }

        if (event.getDrawAt() != null) {
            String drawDateStr = sdf.format(event.getDrawAt().toDate());
            String guidelines =
                    "• Random selection on " + drawDateStr + "\n" +
                            "• Winners notified via app\n" +
                            "• 48 hours to accept invitation\n" +
                            "• Replacements drawn if declined";
            textLotteryGuidelines.setText(guidelines);
        }

        updateStatusUI(event);

        currentPosterBase64 = event.getPosterImage();
        if (currentPosterBase64 != null && !currentPosterBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(currentPosterBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    eventPoster.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                eventPoster.setImageResource(R.drawable.img_poster);
            }
        } else {
            eventPoster.setImageResource(R.drawable.img_poster);
        }
    }

    private void updateStatusUI(Event event) {
        Timestamp now = Timestamp.now();
        String status = event.getStatus() != null ? event.getStatus().toLowerCase() : "open";

        if (event.getEventStartAt() != null && event.getEventStartAt().compareTo(now) < 0) {
            textEventStatus.setText("CLOSED");
            textEventStatus.setTextColor(Color.parseColor("#9CA3AF"));
            if (!"closed".equals(status)) {
                db.collection("events").document(eventId).update("status", "closed");
            }
        } else if ("drawn".equals(status)) {
            textEventStatus.setText("DRAWN");
            textEventStatus.setTextColor(Color.parseColor("#F57C00"));
        } else {
            textEventStatus.setText("ACTIVE");
            textEventStatus.setTextColor(Color.parseColor("#388E3C"));
            if (!"open".equals(status)) {
                db.collection("events").document(eventId).update("status", "open");
            }
        }
    }

    private void showRunLotteryDrawDialog() {
        if (currentEvent == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_run_lottery, null);
        EditText editSampleCount = view.findViewById(R.id.editSampleCount);
        TextView textSampleHint = view.findViewById(R.id.textSampleHint);
        Button btnCancel = view.findViewById(R.id.buttonCancelRunLottery);
        Button btnConfirm = view.findViewById(R.id.buttonConfirmRunLottery);

        List<Event.WaitingListEntry> waitingList = currentEvent.getWaitingList();
        if (waitingList == null) {
            waitingList = new ArrayList<>();
        }

        int capacity = currentEvent.getCapacity();
        int alreadySampled = 0;
        List<Event.WaitingListEntry> eligibleEntrants = new ArrayList<>();

        for (Event.WaitingListEntry entry : waitingList) {
            if (entry == null) continue;

            String status = entry.getParticipationStatus();
            if (status == null) status = "";

            // alreadySampled = selected + accepted only
            if ("selected".equalsIgnoreCase(status) || "accepted".equalsIgnoreCase(status)) {
                alreadySampled++;
            } else {
                // All others are still eligible to be drawn
                eligibleEntrants.add(entry);
            }
        }

        int remainingSlots = Math.max(0, capacity - alreadySampled);
        int waitingListCount = eligibleEntrants.size();
        int maxSampleAllowed = Math.min(remainingSlots, waitingListCount);

        textSampleHint.setText(String.format(
                Locale.getDefault(),
                "Maximum allowed: %d",
                maxSampleAllowed
        ));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (maxSampleAllowed <= 0) {
            btnConfirm.setEnabled(false);
            textSampleHint.setText("No available slots left");
            textSampleHint.setTextColor(Color.RED);
        }

        btnConfirm.setOnClickListener(v -> {
            String input = editSampleCount.getText() == null
                    ? ""
                    : editSampleCount.getText().toString().trim();

            int sampleCount;

            if (input.isEmpty()) {
                sampleCount = maxSampleAllowed;
            } else {
                try {
                    sampleCount = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    editSampleCount.setError("Invalid number");
                    return;
                }

                if (sampleCount <= 0) {
                    editSampleCount.setError("Must be greater than 0");
                    return;
                }

                if (sampleCount > maxSampleAllowed) {
                    editSampleCount.setError("Cannot exceed maximum allowed (" + maxSampleAllowed + ")");
                    return;
                }
            }

            if (sampleCount <= 0) {
                Toast.makeText(this, "No available entrants to sample", Toast.LENGTH_SHORT).show();
                return;
            }

            executeLottery(eligibleEntrants, sampleCount);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void executeLottery(List<Event.WaitingListEntry> eligibleEntrants, int sampleCount) {
        if (sampleCount <= 0 || eligibleEntrants == null || eligibleEntrants.isEmpty()) {
            return;
        }

        Collections.shuffle(eligibleEntrants);

        int actualSampleCount = Math.min(sampleCount, eligibleEntrants.size());
        List<Event.WaitingListEntry> selected = eligibleEntrants.subList(0, actualSampleCount);

        WriteBatch batch = db.batch();

        for (Event.WaitingListEntry entry : selected) {
            entry.setParticipationStatus("selected");
            entry.setSelectedAt(Timestamp.now());

            Map<String, Object> notification = new HashMap<>();
            notification.put("recipientId", entry.getDeviceId());
            notification.put("senderId", deviceId);
            notification.put("recipientType", "ENTRANT");
            notification.put("eventId", eventId);
            notification.put("type", "LOTTERY_WIN");
            notification.put("title", "Lottery Selection");
            notification.put("message", "You have been selected for " + currentEvent.getTitle());
            notification.put("isRead", false);
            notification.put("createdAt", Timestamp.now());
            notification.put("actionStatus", "PENDING");

            batch.set(db.collection("notifications").document(), notification);
        }

        batch.update(
                db.collection("events").document(eventId),
                "waitingList", currentEvent.getWaitingList(),
                "status", "drawn"
        );

        batch.commit()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(
                                this,
                                "Successfully sampled " + selected.size() + " entrants",
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to run lottery: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    @Override
    public void onPosterSelected(Uri uri) {
        String base64Image = uriToBase64(uri);
        if (base64Image != null && eventId != null) {
            db.collection("events").document(eventId)
                    .update("posterImage", base64Image, "updatedAt", Timestamp.now())
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Poster updated successfully", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update poster", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    @Override
    public void onPosterRemoved() {
        if (eventId != null) {
            db.collection("events").document(eventId)
                    .update("posterImage", null, "updatedAt", Timestamp.now())
                    .addOnSuccessListener(aVoid -> {
                        eventPoster.setImageResource(R.drawable.img_poster);
                        Toast.makeText(this, "Poster removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to remove poster", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            int maxWidth = 800;
            int maxHeight = 800;
            if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                float ratio = Math.min(
                        (float) maxWidth / bitmap.getWidth(),
                        (float) maxHeight / bitmap.getHeight()
                );
                int width = Math.round(ratio * bitmap.getWidth());
                int height = Math.round(ratio * bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}