package com.example.lottery_legend;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private EditText editTextEventTitle;
    private EditText editTextDescription;
    private EditText editTextLocation;
    private EditText eventStartDate;
    private EditText eventEndDate;
    private EditText registrationStartDate;
    private EditText registrationEndDate;
    private EditText drawDate;
    private EditText editTextCapacity;
    private EditText editTextWaitingList;
    private SwitchCompat switchGeo;
    private MaterialCardView locationCard;
    private Button createButton;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        toolbar = findViewById(R.id.toolbarCreateEvent);
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        eventStartDate = findViewById(R.id.EventStartDate);
        eventEndDate = findViewById(R.id.EventEndDate);
        registrationStartDate = findViewById(R.id.RegistrationStart);
        registrationEndDate = findViewById(R.id.RegistrationEnd);
        drawDate = findViewById(R.id.etDrawDate);
        editTextCapacity = findViewById(R.id.Capacity);
        editTextWaitingList = findViewById(R.id.WaitingList);
        switchGeo = findViewById(R.id.switchGeo);
        locationCard = findViewById(R.id.locationCard);
        createButton = findViewById(R.id.createButton);

        setupDatePickers();
        setupGeoSwitch();

        toolbar.setNavigationOnClickListener(v -> finish());
        createButton.setOnClickListener(v -> createEvent());
    }

    private void setupGeoSwitch() {
        locationCard.setVisibility(switchGeo.isChecked() ? View.VISIBLE : View.GONE);

        switchGeo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                locationCard.setVisibility(View.VISIBLE);
            } else {
                locationCard.setVisibility(View.GONE);
                editTextLocation.setText("");
            }
        });
    }

    private void setupDatePickers() {
        View.OnClickListener dateListener = v -> {
            EditText et = (EditText) v;
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> et.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year1), year, month, day);
            datePickerDialog.show();
        };

        eventStartDate.setOnClickListener(dateListener);
        eventEndDate.setOnClickListener(dateListener);
        registrationStartDate.setOnClickListener(dateListener);
        registrationEndDate.setOnClickListener(dateListener);
        drawDate.setOnClickListener(dateListener);
    }

    private void createEvent() {
        String title = editTextEventTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String startDateStr = eventStartDate.getText().toString().trim();
        String endDateStr = eventEndDate.getText().toString().trim();
        String regStart = registrationStartDate.getText().toString().trim();
        String regEnd = registrationEndDate.getText().toString().trim();
        String drawD = drawDate.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String waitingListStr = editTextWaitingList.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                (switchGeo.isChecked() && TextUtils.isEmpty(location)) ||
                TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr) || TextUtils.isEmpty(regStart) ||
                TextUtils.isEmpty(regEnd) || TextUtils.isEmpty(drawD) || TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitingList = null;
        if (!TextUtils.isEmpty(waitingListStr)) {
            maxWaitingList = Integer.parseInt(waitingListStr);
        }

        Event newEvent = new Event(deviceId, title, description, switchGeo.isChecked(), location,
                startDateStr, endDateStr, regStart, regEnd, drawD, capacity, maxWaitingList);

        db.collection("events").document(newEvent.getEventId())
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEventActivity.this, "Event Created Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventActivity.this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
