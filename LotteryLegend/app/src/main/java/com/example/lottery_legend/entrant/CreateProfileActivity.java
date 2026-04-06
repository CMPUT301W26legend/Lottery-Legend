package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Entrant;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Activity for creating a new user profile upon first launch.
 * It collects user information such as name, email, phone number, and profile picture,
 * and saves it to the Firestore database.
 */
public class CreateProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private com.google.android.material.switchmaterial.SwitchMaterial switchNotification;
    private Button saveButton;
    private ImageView imgAvatar;
    private TextView tvUploadPhoto;
    private String profileImageBase64;

    /**
     * Launcher for selecting an image from the device's gallery.
     */
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processAndSetImage(uri);
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve unique device ID passed from the previous activity
        deviceId = getIntent().getStringExtra("deviceID");

        nameEditText = findViewById(R.id.etName);
        emailEditText = findViewById(R.id.etEmail);
        phoneEditText = findViewById(R.id.etPhone);
        switchNotification = findViewById(R.id.switchNotification);
        saveButton = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);

        // Set up click listeners for profile picture upload
        tvUploadPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        imgAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phoneRaw = phoneEditText.getText().toString().trim();
            boolean notification = switchNotification.isChecked();

            // Validate mandatory fields
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Invalid email format");
                return;
            }

            // Format and validate phone number if provided
            String phoneFormatted = phoneRaw;
            if (!phoneRaw.isEmpty()) {
                // Strip non-digit characters
                String digits = phoneRaw.replaceAll("\\D", "");
                if (digits.length() == 10) {
                    // Auto-format to XXX-XXX-XXXX
                    phoneFormatted = digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
                    phoneEditText.setText(phoneFormatted);
                }

                if (!isValidCAPhone(phoneFormatted)) {
                    phoneEditText.setError("Invalid phone format. Please use 10 digits (e.g., 123-456-7890)");
                    return;
                }
            }
            
            Timestamp now = Timestamp.now();
            Entrant user = new Entrant(deviceId, name, email, phoneFormatted, notification, now, now, false, profileImageBase64);

            // Save the profile to Firestore under the 'entrants' collection
            db.collection("entrants").document(deviceId).set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                        // Navigate to the main activity after successful creation
                        Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    /**
     * Checks if the given phone number string matches a standard North American format.
     * @param phone The phone number string to validate.
     * @return True if valid, false otherwise.
     */
    private boolean isValidCAPhone(String phone) {
        String regex = "^(\\+?1)?[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        return Pattern.compile(regex).matcher(phone).matches();
    }

    /**
     * Processes the selected image: resizes it if it's too large and converts it to a Base64 string.
     * @param uri The URI of the selected image.
     */
    private void processAndSetImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap != null) {
                // Resize image to keep Firestore document size small
                int maxWidth = 500;
                int maxHeight = 500;
                if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                    float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(ratio * bitmap.getWidth()), Math.round(ratio * bitmap.getHeight()), true);
                }

                // Compress and encode to Base64
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                profileImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                
                // Update UI
                imgAvatar.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
}
