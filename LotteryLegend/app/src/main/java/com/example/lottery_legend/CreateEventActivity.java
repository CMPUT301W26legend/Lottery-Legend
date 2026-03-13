package com.example.lottery_legend;

import android.app.DatePickerDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements PosterUploadDialogFragment.OnPosterEventListener {

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
        uploadButton = findViewById(R.id.uploadButton);

        setupDatePickers();
        setupGeoSwitch();

        toolbar.setNavigationOnClickListener(v -> finish());

        uploadButton.setOnClickListener(v -> {
            PosterUploadDialogFragment dialog = new PosterUploadDialogFragment();
            dialog.setCurrentUri(imageUri);
            dialog.setOnPosterEventListener(this);
            dialog.show(getSupportFragmentManager(), "PosterUploadDialog");
        });

        createButton.setOnClickListener(v -> createEvent());
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

        if (imageUri != null) {
            String base64Image = uriToBase64(imageUri);
            if (base64Image != null) {
                newEvent.setPosterImage(base64Image);
            }
        }
        //
        String qrCodeBase64 = generateQRCodeBase64(newEvent.getEventId());
        newEvent.setQrCodeImage(qrCodeBase64);

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
     * Generates a QR Code for the eventId and returns it as a Base64 string.
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

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            int maxWidth = 800;
            int maxHeight = 800;
            if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
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
