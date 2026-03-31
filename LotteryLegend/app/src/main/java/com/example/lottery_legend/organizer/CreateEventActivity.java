package com.example.lottery_legend.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for organizers to create a new event.
 * This activity handles input validation, poster image selection,
 * QR code generation, and saving event data to Firebase Firestore.
 */
public class CreateEventActivity extends AppCompatActivity implements PosterUploadDialogFragment.OnPosterEventListener {

    private FirebaseFirestore db;
    private String deviceId;
    private EditText editTextEventTitle;
    private EditText editTextDescription;
    private EditText editTextLocation;
    private EditText editTextPrice;
    private EditText eventStartDateTime;
    private EditText eventEndDateTime;
    private EditText registrationStartDateTime;
    private EditText registrationEndDateTime;
    private EditText drawDateTime;
    private EditText editTextCapacity;
    private EditText editTextWaitingList;
    private SwitchCompat switchGeo;
    private SwitchCompat switchPrivateEvent;
    private Button createButton, uploadButton;
    private MaterialToolbar toolbar;

    private Uri imageUri;

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

        // Initialize UI components
        toolbar = findViewById(R.id.toolbarCreateEvent);
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextPrice = findViewById(R.id.editTextPrice);
        eventStartDateTime = findViewById(R.id.eventStartDateTime);
        eventEndDateTime = findViewById(R.id.eventEndDateTime);
        registrationStartDateTime = findViewById(R.id.registrationStartDateTime);
        registrationEndDateTime = findViewById(R.id.registrationEndDateTime);
        drawDateTime = findViewById(R.id.drawDateTime);
        editTextCapacity = findViewById(R.id.Capacity);
        editTextWaitingList = findViewById(R.id.WaitingList);
        switchGeo = findViewById(R.id.switchGeo);
        switchPrivateEvent = findViewById(R.id.switchPrivateEvent);
        createButton = findViewById(R.id.createButton);
        uploadButton = findViewById(R.id.uploadButton);

        // Setup UI logic
        setupDateTimePickers();

        // Toolbar back navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Handle poster image upload button
        uploadButton.setOnClickListener(v -> {
            PosterUploadDialogFragment dialog = new PosterUploadDialogFragment();
            dialog.setCurrentUri(imageUri);
            dialog.setOnPosterEventListener(this);
            dialog.show(getSupportFragmentManager(), "PosterUploadDialog");
        });

        // Handle event creation button
        createButton.setOnClickListener(v -> createEvent());
    }

    /**
     * Callback from PosterUploadDialogFragment when a poster is selected.
     * @param uri The Uri of the selected image.
     */
    @Override
    public void onPosterSelected(Uri uri) {
        this.imageUri = uri;
        uploadButton.setText("Image Selected");
    }

    /**
     * Callback from PosterUploadDialogFragment when the poster is removed.
     */
    @Override
    public void onPosterRemoved() {
        this.imageUri = null;
        uploadButton.setText("Upload Poster Image");
    }

    /**
     * Configures DateTimePickerDialogs for all date/time input fields.
     */
    private void setupDateTimePickers() {
        EditText[] dateFields = {eventStartDateTime, eventEndDateTime, registrationStartDateTime, registrationEndDateTime, drawDateTime};
        for (EditText et : dateFields) {
            et.setOnClickListener(v -> showDateTimePicker(et));
        }
    }

    private void showDateTimePicker(EditText et) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute1) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
                et.setText(sdf.format(selected.getTime()));
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Validates input fields, generates a QR code, and saves the new event to Firestore.
     */
    private void createEvent() {
        String title = editTextEventTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String locationName = editTextLocation.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String startDateStr = eventStartDateTime.getText().toString().trim();
        String endDateStr = eventEndDateTime.getText().toString().trim();
        String regStartStr = registrationStartDateTime.getText().toString().trim();
        String regEndStr = registrationEndDateTime.getText().toString().trim();
        String drawDateStr = drawDateTime.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String waitingListStr = editTextWaitingList.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(locationName) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr) || TextUtils.isEmpty(regStartStr) ||
                TextUtils.isEmpty(regEndStr) || TextUtils.isEmpty(drawDateStr) || TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse numerical fields
        double price = Double.parseDouble(priceStr);
        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitingList = null;
        if (!TextUtils.isEmpty(waitingListStr)) {
            maxWaitingList = Integer.parseInt(waitingListStr);
        }

        // Create the event object
        Event newEvent = new Event();
        newEvent.setOrganizerId(deviceId);
        newEvent.setTitle(title);
        newEvent.setDescription(description);
        newEvent.setPrice(price);
        boolean isPrivate = switchPrivateEvent.isChecked();
        newEvent.setIsPrivateEvent(isPrivate);
        newEvent.setGeoEnabled(switchGeo.isChecked()); // Controls recording entrant's geo
        newEvent.setEventLocation(new Event.EventLocation(locationName, null, null, null));
        newEvent.setCapacity(capacity);
        newEvent.setMaxWaitingList(maxWaitingList);
        newEvent.setStatus("open");
        newEvent.setCreatedAt(Timestamp.now());
        newEvent.setUpdatedAt(Timestamp.now());

        // Parse date strings and validate chronological order
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        try {
            Date regStart = sdf.parse(regStartStr);
            Date regEnd = sdf.parse(regEndStr);
            Date drawDate = sdf.parse(drawDateStr);
            Date eventStart = sdf.parse(startDateStr);
            Date eventEnd = sdf.parse(endDateStr);

            if (regStart.after(regEnd)) {
                Toast.makeText(this, "Registration start must be before end", Toast.LENGTH_SHORT).show();
                return;
            }
            if (regEnd.after(drawDate)) {
                Toast.makeText(this, "Registration end must be before draw date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (drawDate.after(eventStart)) {
                Toast.makeText(this, "Draw date must be before event start", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventStart.after(eventEnd)) {
                Toast.makeText(this, "Event start must be before end", Toast.LENGTH_SHORT).show();
                return;
            }

            newEvent.setRegistrationStartAt(new Timestamp(regStart));
            newEvent.setRegistrationEndAt(new Timestamp(regEnd));
            newEvent.setDrawAt(new Timestamp(drawDate));
            newEvent.setEventStartAt(new Timestamp(eventStart));
            newEvent.setEventEndAt(new Timestamp(eventEnd));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a random eventId
        newEvent.setEventId(java.util.UUID.randomUUID().toString());

        // Process poster image if available
        if (imageUri != null) {
            String base64Image = uriToBase64(imageUri);
            if (base64Image != null) {
                newEvent.setPosterImage(base64Image);
            }
        }
        
        // Generate and set QR code for the event if not private
        if (!isPrivate) {
            String qrCodeBase64 = generateQRCodeBase64(newEvent.getEventId());
            newEvent.setQrCodeImage(qrCodeBase64);
            newEvent.setQrCodeValue(newEvent.getEventId());
        } else {
            newEvent.setQrCodeImage(null);
            newEvent.setQrCodeValue(null);
        }

        // Save to Firestore
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

    /**
     * Generates a QR Code for the given text and returns it as a Base64 string.
     * @param text The text to encode in the QR code (usually eventId).
     * @return Base64 encoded PNG string of the QR code, or null if generation fails.
     */
    private String generateQRCodeBase64(String text) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts an image Uri to a Base64 encoded JPEG string.
     * Resizes the image to fit within 800x800 for efficient storage.
     * @param uri The Uri of the image to convert.
     * @return Base64 encoded string of the image, or null on error.
     */
    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            // Downscale image if it's too large for Firestore
            int maxWidth = 800;
            int maxHeight = 800;
            if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
                int width = Math.round(ratio * bitmap.getWidth());
                int height = Math.round(ratio * bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            // Compress to JPEG with 50% quality to further reduce size
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
