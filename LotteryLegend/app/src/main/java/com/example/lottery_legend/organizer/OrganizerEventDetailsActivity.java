package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.MapActivity;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Activity for organizers to view the details of a specific event they have created.
 * Includes the state machine for Lottery and Replacement draws.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity implements PosterUploadDialogFragment.OnPosterEventListener {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    private ImageView eventPoster, imageOrganizerProfile;
    private TextView textWaitingCount, textSelectedCount, textCapacity;
    private TextView textEventTitle, textEventStatus, tagCoOrganizer, textEventVisibility;
    private TextView textEventDate, textRegistrationDeadline, textLocation, textPrice;
    private TextView textDescription, textLotteryGuidelines, textOrganizerName;
    private Button btnViewWaitingList, btnRunLotteryDraw, btnSendNotification, btnDeleteEvent, btnInviteEntrants;
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
        imageOrganizerProfile = findViewById(R.id.imageOrganizerProfile);
        textWaitingCount = findViewById(R.id.textWaitingCount);
        textSelectedCount = findViewById(R.id.textSelectedCount);
        textCapacity = findViewById(R.id.textCapacity);
        textEventTitle = findViewById(R.id.textEventTitle);
        tagCoOrganizer = findViewById(R.id.tagCoOrganizer);
        textEventStatus = findViewById(R.id.textEventStatus);
        textEventVisibility = findViewById(R.id.textEventVisibility);
        textEventDate = findViewById(R.id.textEventDate);
        textRegistrationDeadline = findViewById(R.id.textRegistrationDeadline);
        textLocation = findViewById(R.id.textLocation);
        textPrice = findViewById(R.id.textPrice);
        textDescription = findViewById(R.id.textDescription);
        textLotteryGuidelines = findViewById(R.id.textLotteryGuidelines);
        textOrganizerName = findViewById(R.id.textOrganizerName);

        btnViewWaitingList = findViewById(R.id.btnViewWaitingList);
        btnRunLotteryDraw = findViewById(R.id.btnRunLotteryDraw);
        btnSendNotification = findViewById(R.id.btnSendNotification);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnInviteEntrants = findViewById(R.id.btnInviteEntrants);
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

        btnRunLotteryDraw.setOnClickListener(v -> handleLotteryClick());

        btnSendNotification.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, SendNotificationActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
        });

        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmationDialog());

        btnInviteEntrants.setOnClickListener(v -> showInviteSearchDialog());
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

        // Show Co-organizer tag if the current user is not the primary organizer
        if (event.getOrganizerId() != null && !event.getOrganizerId().equals(deviceId)) {
            tagCoOrganizer.setVisibility(View.VISIBLE);
        } else {
            tagCoOrganizer.setVisibility(View.GONE);
        }

        // Fetch organizer details (name and profile image)
        if (event.getOrganizerId() != null) {
            db.collection("organizers").document(event.getOrganizerId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    textOrganizerName.setText(doc.getString("name"));
                    String profileImage = doc.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        displayBase64Image(profileImage, imageOrganizerProfile);
                    } else {
                        imageOrganizerProfile.setImageResource(R.drawable.ic_profile_avatar);
                    }
                }
            });
        }

        int waitingListSize = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        if (event.getMaxWaitingList() != null) {
            textWaitingCount.setText(String.format(Locale.getDefault(), "%d/%d", waitingListSize, event.getMaxWaitingList()));
        } else {
            textWaitingCount.setText(String.valueOf(waitingListSize));
        }

        // Display current selected count (Occupied spots)
        textSelectedCount.setText(String.valueOf(getOccupiedCount()));

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
        updateLotteryButtonState();

        // Business Logic: Trigger First Draw if current time >= drawAt and status is not DRAWN
        if (event.getDrawAt() != null && event.getDrawAt().compareTo(Timestamp.now()) <= 0 && !"drawn".equalsIgnoreCase(event.getStatus())) {
            List<Event.WaitingListEntry> candidates = getWaitingEntrants();
            if (!candidates.isEmpty()) {
                executeLottery(candidates, event.getCapacity(), true);
            }
        }

        if (event.isIsPrivateEvent()) {
            btnInviteEntrants.setVisibility(View.VISIBLE);
            textEventVisibility.setVisibility(View.VISIBLE);
        } else {
            btnInviteEntrants.setVisibility(View.GONE);
            textEventVisibility.setVisibility(View.GONE);
        }

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

    private void updateStatusUI(Event event) {
        Timestamp now = Timestamp.now();
        String status = event.getStatus() != null ? event.getStatus().toLowerCase() : "open";

        if (event.getEventStartAt() != null && event.getEventStartAt().compareTo(now) < 0) {
            textEventStatus.setText("CLOSED");
            textEventStatus.setTextColor(Color.parseColor("#9CA3AF"));
            if (!"closed".equals(status)) {
                db.collection("events").document(eventId).update("status", "closed");
            }
        } else if ("finalized".equals(status)) {
            textEventStatus.setText("FINALIZED");
            textEventStatus.setTextColor(Color.parseColor("#16A34A"));
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

    /**
     * Updates the "Draw Lottery" button text and enabled state based on the current event state.
     */
    private void updateLotteryButtonState() {
        if (currentEvent == null) return;

        String status = currentEvent.getStatus() != null ? currentEvent.getStatus().toLowerCase() : "";
        int slots = getAvailableSlots();
        boolean hasWaiting = !getWaitingEntrants().isEmpty();

        if ("finalized".equals(status)) {
            btnRunLotteryDraw.setText("Export Attendee List");
            btnRunLotteryDraw.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16A34A")));
            btnRunLotteryDraw.setEnabled(true);
            btnRunLotteryDraw.setAlpha(1.0f);
        } else if (!"drawn".equals(status)) {
            // State: INITIAL
            btnRunLotteryDraw.setText("Draw Lottery");
            btnRunLotteryDraw.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
            btnRunLotteryDraw.setEnabled(true);
            btnRunLotteryDraw.setAlpha(1.0f);
        } else {
            if (slots > 0) {
                // State: REPLACEMENT AVAILABLE or EXHAUSTED
                btnRunLotteryDraw.setText("Replacement Draw");
                btnRunLotteryDraw.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
                if (hasWaiting) {
                    btnRunLotteryDraw.setEnabled(true);
                    btnRunLotteryDraw.setAlpha(1.0f);
                } else {
                    btnRunLotteryDraw.setEnabled(false);
                    btnRunLotteryDraw.setAlpha(0.5f);
                }
            } else {
                // State: LOCKED (DRAWN, but no vacancies)
                btnRunLotteryDraw.setText("Draw Lottery");
                btnRunLotteryDraw.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
                btnRunLotteryDraw.setEnabled(false);
                btnRunLotteryDraw.setAlpha(0.5f);
            }
        }
    }

    /**
     * Logic for the Draw button click.
     */
    private void handleLotteryClick() {
        if (currentEvent == null) return;

        String status = currentEvent.getStatus() != null ? currentEvent.getStatus().toLowerCase() : "";
        
        if ("finalized".equals(status)) {
            exportFinalAttendeeListToCsv();
            return;
        }

        List<Event.WaitingListEntry> candidates = getWaitingEntrants();

        if (candidates.isEmpty()) {
            Toast.makeText(this, "No entrants in waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"drawn".equals(status)) {
            // First Draw
            showDrawDialog(candidates, currentEvent.getCapacity(), true);
        } else {
            // Replacement Draw
            int slots = getAvailableSlots();
            if (slots > 0) {
                showDrawDialog(candidates, slots, false);
            } else {
                Toast.makeText(this, "No available slots for replacement", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportFinalAttendeeListToCsv() {
        if (currentEvent == null || currentEvent.getWaitingList() == null) return;

        List<String> winnerIds = new ArrayList<>();
        for (Event.WaitingListEntry entry : currentEvent.getWaitingList()) {
            if ("WIN".equals(entry.getFinalResult())) {
                winnerIds.add(entry.getDeviceId());
            }
        }

        if (winnerIds.isEmpty()) {
            Toast.makeText(this, "No finalized winners to export", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String wid : winnerIds) {
            tasks.add(db.collection("entrants").document(wid).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            StringBuilder csv = new StringBuilder("Name,Email\n");
            for (Object obj : results) {
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    csv.append(name != null ? name : "N/A").append(",")
                       .append(email != null ? email : "N/A").append("\n");
                }
            }
            saveAndShareCsv(csv.toString());
        }).addOnFailureListener(e -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveAndShareCsv(String content) {
        try {
            File file = new File(getExternalCacheDir(), "AttendeeList_" + eventId + ".csv");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Attendee List"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDrawDialog(List<Event.WaitingListEntry> candidates, int maxAllowed, boolean isFirstDraw) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_run_lottery, null);
        EditText editSampleCount = view.findViewById(R.id.editSampleCount);
        TextView textSampleHint = view.findViewById(R.id.textSampleHint);
        Button btnCancel = view.findViewById(R.id.buttonCancelRunLottery);
        Button btnConfirm = view.findViewById(R.id.buttonConfirmRunLottery);

        int countToPick = Math.min(maxAllowed, candidates.size());
        textSampleHint.setText(String.format(Locale.getDefault(), "Picking from %d waiting entrants. Max allowed: %d", candidates.size(), maxAllowed));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String input = editSampleCount.getText().toString().trim();
            int pickCount = input.isEmpty() ? countToPick : Integer.parseInt(input);

            if (pickCount <= 0 || pickCount > countToPick) {
                editSampleCount.setError("Invalid number");
                return;
            }

            executeLottery(candidates, pickCount, isFirstDraw);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void executeLottery(List<Event.WaitingListEntry> candidates, int sampleCount, boolean isFirstDraw) {
        if (sampleCount <= 0 || candidates.isEmpty()) return;

        Collections.shuffle(candidates);
        List<Event.WaitingListEntry> selected = candidates.subList(0, Math.min(sampleCount, candidates.size()));
        List<String> selectedDeviceIds = new ArrayList<>();
        for (Event.WaitingListEntry entry : selected) {
            selectedDeviceIds.add(entry.getDeviceId());
        }

        // Rule 13: Check notification preference
        db.collection("entrants")
                .whereIn("deviceId", selectedDeviceIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Boolean> notificationMap = new HashMap<>();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            notificationMap.put(entrant.getDeviceId(), entrant.isNotificationsEnabled());
                        }
                    }

                    WriteBatch batch = db.batch();
                    Timestamp now = Timestamp.now();

                    for (Event.WaitingListEntry entry : selected) {
                        entry.setParticipationStatus("selected");
                        entry.setSelectedAt(now);
                        entry.setUpdatedAt(now);

                        // Rule 6: Respect notificationsEnabled (Only skip notification, still update entry)
                        boolean canNotify = notificationMap.getOrDefault(entry.getDeviceId(), true);
                        if (canNotify) {
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("notificationId", db.collection("notifications").document().getId());
                            notification.put("recipientId", entry.getDeviceId());
                            notification.put("senderId", deviceId);
                            notification.put("recipientType", "ENTRANT");
                            notification.put("eventId", eventId);
                            notification.put("type", "LOTTERY_WIN"); // Rule 2
                            notification.put("title", "Lottery Selection");
                            notification.put("message", "You have been selected for " + currentEvent.getTitle());
                            notification.put("isRead", false);
                            notification.put("createdAt", now);
                            notification.put("actionStatus", "PENDING"); // Rule 3

                            batch.set(db.collection("notifications").document((String)notification.get("notificationId")), notification);
                        }
                    }

                    Map<String, Object> eventUpdates = new HashMap<>();
                    eventUpdates.put("waitingList", currentEvent.getWaitingList());
                    if (isFirstDraw) {
                        eventUpdates.put("status", "drawn");
                    }
                    eventUpdates.put("updatedAt", now);

                    batch.update(db.collection("events").document(eventId), eventUpdates);

                    batch.commit().addOnSuccessListener(aVoid -> {
                        String msg = isFirstDraw ? "First draw completed" : "Replacement draw completed";
                        Toast.makeText(this, msg + ": " + selected.size() + " entrants picked", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    /**
     * Calculates occupied count: SELECTED, INVITED, ACCEPTED, ENROLLED, CONFIRMED.
     */
    private int getOccupiedCount() {
        if (currentEvent == null || currentEvent.getWaitingList() == null) return 0;
        int count = 0;
        for (Event.WaitingListEntry entry : currentEvent.getWaitingList()) {
            String status = entry.getParticipationStatus() != null ? entry.getParticipationStatus().toLowerCase() : "";
            if (status.equals("selected") || status.equals("invited") || status.equals("accepted") || status.equals("enrolled") || status.equals("confirmed")) {
                count++;
            }
        }
        return count;
    }

    private int getAvailableSlots() {
        if (currentEvent == null) return 0;
        return Math.max(0, currentEvent.getCapacity() - getOccupiedCount());
    }

    private List<Event.WaitingListEntry> getWaitingEntrants() {
        List<Event.WaitingListEntry> candidates = new ArrayList<>();
        if (currentEvent == null || currentEvent.getWaitingList() == null) return candidates;
        for (Event.WaitingListEntry entry : currentEvent.getWaitingList()) {
            if ("waiting".equalsIgnoreCase(entry.getParticipationStatus())) {
                candidates.add(entry);
            }
        }
        return candidates;
    }

    private void showInviteSearchDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_invite_entrants_search, null);
        EditText editName = view.findViewById(R.id.editSearchName);
        EditText editEmail = view.findViewById(R.id.editSearchEmail);
        EditText editPhone = view.findViewById(R.id.editSearchPhone);
        Button btnCancel = view.findViewById(R.id.btnCancelSearch);
        Button btnSearch = view.findViewById(R.id.btnPerformSearch);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSearch.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            if (name.isEmpty() && email.isEmpty() && phone.isEmpty()) {
                Toast.makeText(this, "Please enter at least one search criterion", Toast.LENGTH_SHORT).show();
                return;
            }

            performEntrantSearch(name, email, phone);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performEntrantSearch(String name, String email, String phone) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Create prefix-range queries for non-empty fields
        if (!name.isEmpty()) {
            tasks.add(db.collection("entrants").orderBy("name").startAt(name).endAt(name + "\uf8ff").get());
        }
        if (!email.isEmpty()) {
            // Case Sensitivity: ensure lowercase for email if database requirement exists
            String emailSearch = email.toLowerCase();
            tasks.add(db.collection("entrants").orderBy("email").startAt(emailSearch).endAt(emailSearch + "\uf8ff").get());
        }
        if (!phone.isEmpty()) {
            tasks.add(db.collection("entrants").orderBy("phone").startAt(phone).endAt(phone + "\uf8ff").get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
            Map<String, Entrant> uniqueResults = new HashMap<>();
            for (Object item : list) {
                QuerySnapshot snapshot = (QuerySnapshot) item;
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Entrant entrant = doc.toObject(Entrant.class);
                    if (entrant != null) {
                        uniqueResults.put(entrant.getDeviceId(), entrant);
                    }
                }
            }

            List<Entrant> results = new ArrayList<>(uniqueResults.values());
            if (results.isEmpty()) {
                Toast.makeText(this, "No entrants found matching criteria", Toast.LENGTH_SHORT).show();
            } else {
                showSearchResultsDialog(results);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showSearchResultsDialog(List<Entrant> results) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_invite_search_results, null);
        RecyclerView recycler = view.findViewById(R.id.recyclerSearchResults);
        Button btnBack = view.findViewById(R.id.btnBackToSearch);
        Button btnInvite = view.findViewById(R.id.btnInviteSelected);
        ImageButton btnClose = view.findViewById(R.id.btnCloseResults);

        Map<String, String> entrantStatuses = new HashMap<>();
        if (currentEvent != null && currentEvent.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : currentEvent.getWaitingList()) {
                if (entry != null && entry.getDeviceId() != null) {
                    entrantStatuses.put(entry.getDeviceId(), entry.getParticipationStatus());
                }
            }
        }

        InviteSearchAdapter adapter = new InviteSearchAdapter(results, entrantStatuses);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            showInviteSearchDialog();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnInvite.setOnClickListener(v -> {
            List<String> selectedIds = adapter.getSelectedEntrantIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "No entrants selected", Toast.LENGTH_SHORT).show();
                return;
            }
            inviteSelectedEntrants(selectedIds);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void inviteSelectedEntrants(List<String> entrantIds) {
        if (currentEvent == null || eventId == null) return;

        db.collection("entrants")
                .whereIn("deviceId", entrantIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Boolean> notificationMap = new HashMap<>();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            notificationMap.put(entrant.getDeviceId(), entrant.isNotificationsEnabled());
                        }
                    }

                    List<Event.WaitingListEntry> waitingList = currentEvent.getWaitingList();
                    if (waitingList == null) waitingList = new ArrayList<>();

                    WriteBatch batch = db.batch();
                    Timestamp now = Timestamp.now();
                    boolean isPrivate = currentEvent.isIsPrivateEvent();

                    for (String eid : entrantIds) {
                        Event.WaitingListEntry entry = new Event.WaitingListEntry();
                        entry.setDeviceId(eid);
                        entry.setJoinedAt(now);
                        entry.setUpdatedAt(now);
                        entry.setInviteSentAt(now);
                        // Project Rule: set participationStatus = "invited" for private invites
                        entry.setParticipationStatus("invited");
                        waitingList.add(entry);

                        // Rule 6: Respect notificationsEnabled
                        boolean canNotify = notificationMap.getOrDefault(eid, true);
                        if (canNotify) {
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("recipientId", eid);
                            notification.put("senderId", deviceId);
                            notification.put("recipientType", "ENTRANT");
                            notification.put("eventId", eventId);
                            notification.put("type", isPrivate ? "PRIVATE_EVENT_INVITE" : "INVITATION");
                            notification.put("title", isPrivate ? "Private Event Invitation" : "Event Invitation");
                            notification.put("message", isPrivate
                                    ? "You have been privately invited to join " + currentEvent.getTitle()
                                    : "You have been invited to join the waiting list for " + currentEvent.getTitle());
                            notification.put("isRead", false);
                            notification.put("createdAt", now);
                            notification.put("actionStatus", "PENDING");

                            batch.set(db.collection("notifications").document(), notification);
                        }
                    }

                    batch.update(db.collection("events").document(eventId), "waitingList", waitingList);

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Invites sent successfully", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send invites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
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
