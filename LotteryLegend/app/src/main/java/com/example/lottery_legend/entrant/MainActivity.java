package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.EventAdapter;
import com.example.lottery_legend.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The main activity of the application for entrants.
 * Provides searching and filtering of events based on event start date,
 * time of day, and capacity.
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private RecyclerView eventView;
    private EventAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();

    // Filter states
    private String searchQuery = "";
    private Long startDateFilter = null;
    private Long endDateFilter = null;
    private String timeFilter = "Any Time";
    private String capacityFilter = "Capacity";

    private MaterialButton btnDateFilter, btnTimeFilter, btnCapacityFilter;
    private EditText searchInput;
    private FrameLayout layoutNotification;
    private TextView tvNotificationBadge;

    private final SimpleDateFormat displayFormatShort = new SimpleDateFormat("MMM dd", Locale.getDefault());
    
    private boolean notificationsEnabled = true;
    private ListenerRegistration profileListener;
    private ListenerRegistration badgeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Normalize timezones
        displayFormatShort.setTimeZone(TimeZone.getTimeZone("UTC"));

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        setupViews();
        setupListeners();
        fetchEvents();
        observeNotificationPreference();

        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.HOME);
    }

    /**
     * Initializes the UI components and sets up edge-to-edge display logic.
     */
    private void setupViews() {
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnDateFilter = findViewById(R.id.btnDateFilter);
        btnTimeFilter = findViewById(R.id.btnTimeFilter);
        btnCapacityFilter = findViewById(R.id.btnCapacityFilter);
        searchInput = findViewById(R.id.searchInput);
        layoutNotification = findViewById(R.id.layoutNotification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

        eventView = findViewById(R.id.eventView);
        eventView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(filteredEvents, deviceId);
        eventView.setAdapter(adapter);

        updateFiltersUI();
    }

    /**
     * Sets up click listeners and text watchers for interactive UI elements.
     */
    private void setupListeners() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnDateFilter.setOnClickListener(v -> showDateFilterDialog());
        btnTimeFilter.setOnClickListener(v -> showTimeFilterDialog());
        btnCapacityFilter.setOnClickListener(v -> showCapacityFilterDialog());

        layoutNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Listens for changes in the user's notification preferences from Firestore.
     */
    private void observeNotificationPreference() {
        if (deviceId == null) return;

        profileListener = db.collection("entrants").document(deviceId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) return;
                    if (doc != null && doc.exists()) {
                        Boolean enabled = doc.getBoolean("notificationsEnabled");
                        notificationsEnabled = enabled != null ? enabled : true;
                        
                        // Restart badge listener when preference changes
                        setupNotificationBadge();
                    }
                });
    }

    /**
     * Sets up a real-time listener for unread notifications to update the badge count.
     */
    private void setupNotificationBadge() {
        if (deviceId == null) return;

        if (badgeListener != null) {
            badgeListener.remove();
        }

        // If notifications are disabled, hide the badge immediately and don't listen
        if (!notificationsEnabled) {
            tvNotificationBadge.setVisibility(View.GONE);
            return;
        }

        badgeListener = db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        tvNotificationBadge.setVisibility(View.GONE);
                        return;
                    }
                    
                    // Double check preference inside listener
                    if (!notificationsEnabled) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) profileListener.remove();
        if (badgeListener != null) badgeListener.remove();
    }

    /**
     * Shows a dialog to choose between specific day or date range filtering.
     */
    private void showDateFilterDialog() {
        String[] modes = {"Specific Day", "Date Range"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("When should the event start?")
                .setItems(modes, (dialog, which) -> launchPicker(which == 1))
                .show();
    }

    /**
     * Launches the appropriate Material Design date picker.
     * @param isRangeMode True for range picker, false for single date picker.
     */
    private void launchPicker(boolean isRangeMode) {
        if (isRangeMode) {
            MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Starting between...")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                startDateFilter = selection.first;
                endDateFilter = selection.second;
                updateFiltersUI();
            });
            picker.show(getSupportFragmentManager(), "RANGE_PICKER");
        } else {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Starting on...")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                startDateFilter = selection;
                endDateFilter = null;
                updateFiltersUI();
            });
            picker.show(getSupportFragmentManager(), "SINGLE_PICKER");
        }
    }

    /**
     * Shows a dialog to filter events by time of day.
     */
    private void showTimeFilterDialog() {
        String[] options = {"Any Time", "Morning (06:00 - 12:00)", "Afternoon (12:01 - 18:00)", "Night (18:01 - 00:00)", "Midnight (00:00 - 06:00)"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Time of Day")
                .setItems(options, (dialog, which) -> {
                    timeFilter = options[which];
                    updateFiltersUI();
                })
                .show();
    }

    /**
     * Shows a dialog to filter events by their capacity.
     */
    private void showCapacityFilterDialog() {
        String[] options = {"Any", "< 50", "50 – 100", "100+"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Capacity")
                .setItems(options, (dialog, which) -> {
                    capacityFilter = which == 0 ? "Capacity" : options[which];
                    updateFiltersUI();
                })
                .show();
    }

    /**
     * Updates the visual state of filter buttons based on active selections.
     */
    private void updateFiltersUI() {
        // Date Button
        String dateLabel = "Start Date";
        boolean dateActive = false;
        if (startDateFilter != null) {
            dateActive = true;
            if (endDateFilter == null) {
                dateLabel = displayFormatShort.format(new Date(startDateFilter));
            } else {
                dateLabel = displayFormatShort.format(new Date(startDateFilter)) + " – " +
                        displayFormatShort.format(new Date(endDateFilter));
            }
        }
        updateFilterButton(btnDateFilter, dateLabel, dateActive, v -> {
            startDateFilter = null;
            endDateFilter = null;
            updateFiltersUI();
        });

        // Time Button
        updateFilterButton(btnTimeFilter, timeFilter, !timeFilter.equals("Any Time"), v -> {
            timeFilter = "Any Time";
            updateFiltersUI();
        });

        // Capacity Button
        updateFilterButton(btnCapacityFilter, capacityFilter, !capacityFilter.equals("Capacity"), v -> {
            capacityFilter = "Capacity";
            updateFiltersUI();
        });

        applyFilters();
    }

    /**
     * Configures individual filter button colors, icons, and behavior.
     */
    private void updateFilterButton(MaterialButton button, String text, boolean isSelected, View.OnClickListener clearAction) {
        button.setText(text);
        if (isSelected) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
            button.setTextColor(Color.WHITE);
            button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#2563EB")));
            button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
            button.setIconTint(ColorStateList.valueOf(Color.WHITE));
            button.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            button.setIconPadding(8);

            button.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int iconWidth = button.getIcon() != null ? button.getIcon().getIntrinsicWidth() + button.getIconPadding() + button.getPaddingEnd() : 0;
                    int clearZoneWidth = Math.max(iconWidth, (int) (48 * getResources().getDisplayMetrics().density));
                    if (event.getX() > (button.getWidth() - clearZoneWidth)) {
                        clearAction.onClick(v);
                        return true;
                    }
                }
                return false;
            });
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            button.setTextColor(Color.parseColor("#111827"));
            button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D1D5DB")));
            
            if (button.getId() == R.id.btnTimeFilter || button.getId() == R.id.btnCapacityFilter) {
                button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_down_small));
                button.setIconTint(ColorStateList.valueOf(Color.parseColor("#6B7280")));
                button.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            } else {
                button.setIcon(null);
            }
            button.setOnTouchListener(null);
        }
    }

    /**
     * Filters the list of all events based on the current search query and active filter settings.
     */
    private void applyFilters() {
        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matchesSearch = searchQuery.isEmpty()
                    || (event.getTitle() != null
                    && event.getTitle().toLowerCase().contains(searchQuery));

            boolean matchesDate = true;
            if (startDateFilter != null) {
                Timestamp eventStartAt = event.getEventStartAt();
                if (eventStartAt == null) {
                    matchesDate = false;
                } else {
                    long eventTimeUtc = normalizeToUtcStartOfDay(eventStartAt.toDate().getTime());
                    long rangeStartUtc = normalizeToUtcStartOfDay(startDateFilter);

                    if (endDateFilter == null) {
                        matchesDate = (eventTimeUtc == rangeStartUtc);
                    } else {
                        long rangeEndUtc = normalizeToUtcStartOfDay(endDateFilter);
                        matchesDate = (eventTimeUtc >= rangeStartUtc && eventTimeUtc <= rangeEndUtc);
                    }
                }
            }

            boolean matchesTime = true;
            if (!timeFilter.equals("Any Time")) {
                Timestamp eventStartAt = event.getEventStartAt();
                if (eventStartAt == null) {
                    matchesTime = false;
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(eventStartAt.toDate());
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);
                    double timeValue = hour + (minute / 60.0);

                    if (timeFilter.startsWith("Morning")) {
                        matchesTime = (timeValue >= 6.0 && timeValue <= 12.0);
                    } else if (timeFilter.startsWith("Afternoon")) {
                        matchesTime = (timeValue > 12.0 && timeValue <= 18.0);
                    } else if (timeFilter.startsWith("Night")) {
                        matchesTime = (timeValue > 18.0 || (hour == 0 && minute == 0));
                    } else if (timeFilter.startsWith("Midnight")) {
                        matchesTime = (timeValue >= 0.0 && timeValue <= 6.0);
                    }
                }
            }

            boolean matchesCapacity = true;
            if (!capacityFilter.equals("Capacity")) {
                int cap = event.getCapacity();
                if (capacityFilter.equals("< 50")) {
                    matchesCapacity = (cap < 50);
                } else if (capacityFilter.equals("50 – 100")) {
                    matchesCapacity = (cap >= 50 && cap <= 100);
                } else if (capacityFilter.equals("100+")) {
                    matchesCapacity = (cap > 100);
                }
            }

            if (matchesSearch && matchesDate && matchesTime && matchesCapacity) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Normalizes a timestamp to the start of the day in UTC.
     */
    private long normalizeToUtcStartOfDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Fetches events from Firestore and listens for updates.
     */
    private void fetchEvents() {
        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null) return;
            allEvents.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    if (event != null && event.getOrganizerId() != null && !event.getOrganizerId().equals(deviceId)) {
                        // Check if device is a co-organizer
                        doc.getReference().collection("coOrganizers").document(deviceId).get()
                                .addOnSuccessListener(coDoc -> {
                                    if (!coDoc.exists()) {
                                        allEvents.add(event);
                                        applyFilters();
                                    }
                                });
                    }
                }
            }
            applyFilters();
        });
    }
}
