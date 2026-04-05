package com.example.lottery_legend.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.MapActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for organizers to create a new event.
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
    private View locationButton;
    private MaterialToolbar toolbar;

    private Uri imageUri;
    private Double selectedLat = null;
    private Double selectedLng = null;

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra(MapActivity.RESULT_LATITUDE, 0);
                    selectedLng = result.getData().getDoubleExtra(MapActivity.RESULT_LONGITUDE, 0);
                    String address = result.getData().getStringExtra(MapActivity.RESULT_ADDRESS);
                    if (address != null) {
                        editTextLocation.setText(address);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> placesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    editTextLocation.setText(place.getAddress());
                    if (place.getLatLng() != null) {
                        selectedLat = place.getLatLng().latitude;
                        selectedLng = place.getLatLng().longitude;
                    }
                } else if (result.getResultCode() == 2 /* AutocompleteActivity.RESULT_ERROR */) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
    );

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

        initPlaces();
        initViews();
        setupDateTimePickers();
        setupListeners();
    }

    private void initPlaces() {
        if (!Places.isInitialized()) {
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                String apiKey = bundle.getString("com.google.android.geo.API_KEY");
                if (apiKey != null) {
                    Places.initialize(getApplicationContext(), apiKey);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
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
        locationButton = findViewById(R.id.locationButton);

        // Make location edit text look like a search bar trigger
        editTextLocation.setFocusable(false);
        editTextLocation.setClickable(true);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        editTextLocation.setOnClickListener(v -> startAutocompleteIntent());

        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra(MapActivity.EXTRA_PICK_MODE, true);
            if (selectedLat != null && selectedLng != null) {
                intent.putExtra(MapActivity.EXTRA_LATITUDE, (double) selectedLat);
                intent.putExtra(MapActivity.EXTRA_LONGITUDE, (double) selectedLng);
            }
            mapPickerLauncher.launch(intent);
        });

        uploadButton.setOnClickListener(v -> {
            PosterUploadDialogFragment dialog = new PosterUploadDialogFragment();
            dialog.setCurrentUri(imageUri);
            dialog.setOnPosterEventListener(this);
            dialog.show(getSupportFragmentManager(), "PosterUploadDialog");
        });

        createButton.setOnClickListener(v -> createEvent());
    }

    private void startAutocompleteIntent() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        placesLauncher.launch(intent);
    }

    @Override
    public void onPosterSelected(Uri uri) {
        this.imageUri = uri;
        uploadButton.setText("Image Selected");
    }

    @Override
    public void onPosterRemoved() {
        this.imageUri = null;
        uploadButton.setText("Upload Poster Image");
    }

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

        double price = Double.parseDouble(priceStr);
        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitingList = null;
        if (!TextUtils.isEmpty(waitingListStr)) {
            maxWaitingList = Integer.parseInt(waitingListStr);
        }

        Event newEvent = new Event();
        newEvent.setOrganizerId(deviceId);
        newEvent.setTitle(title);
        newEvent.setDescription(description);
        newEvent.setPrice(price);
        newEvent.setIsPrivateEvent(switchPrivateEvent.isChecked());
        newEvent.setGeoEnabled(switchGeo.isChecked());
        
        // Save both Name and Coordinates
        newEvent.setEventLocation(new Event.EventLocation(locationName, locationName, selectedLat, selectedLng));
        
        newEvent.setCapacity(capacity);
        newEvent.setMaxWaitingList(maxWaitingList);
        newEvent.setStatus("open");
        newEvent.setCreatedAt(Timestamp.now());
        newEvent.setUpdatedAt(Timestamp.now());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        try {
            newEvent.setRegistrationStartAt(new Timestamp(sdf.parse(regStartStr)));
            newEvent.setRegistrationEndAt(new Timestamp(sdf.parse(regEndStr)));
            newEvent.setDrawAt(new Timestamp(sdf.parse(drawDateStr)));
            newEvent.setEventStartAt(new Timestamp(sdf.parse(startDateStr)));
            newEvent.setEventEndAt(new Timestamp(sdf.parse(endDateStr)));
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show();
            return;
        }

        newEvent.setEventId(java.util.UUID.randomUUID().toString());

        if (imageUri != null) {
            String base64Image = uriToBase64(imageUri);
            if (base64Image != null) newEvent.setPosterImage(base64Image);
        }
        
        if (!newEvent.isIsPrivateEvent()) {
            newEvent.setQrCodeValue(newEvent.getEventId());
            newEvent.setQrCodeImage(generateQRCodeBase64(newEvent.getEventId()));
        }

        db.collection("events").document(newEvent.getEventId())
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

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
            return null;
        }
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            int maxWidth = 800;
            int maxHeight = 800;
            if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(ratio * bitmap.getWidth()), Math.round(ratio * bitmap.getHeight()), true);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
}
