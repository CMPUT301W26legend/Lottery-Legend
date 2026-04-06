package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.lottery_legend.organizer.NavbarOrganizer;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Activity for entrants and organizers to edit their profile information.
 */
public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private boolean isOrganizerMode = false;

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private ImageView imgAvatar;
    private TextView tvUploadPhoto;
    private Button saveButton;
    private TextView toolbarRoleText;

    private String profileImageBase64;

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
        setContentView(R.layout.activity_edit_profile);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");
        isOrganizerMode = getIntent().getBooleanExtra("isOrganizerMode", false);

        editTextName = findViewById(R.id.etName);
        editTextEmail = findViewById(R.id.etEmail);
        editTextPhone = findViewById(R.id.etPhone);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);
        saveButton = findViewById(R.id.btnSave);
        toolbarRoleText = findViewById(R.id.toolbarRoleText);

        updateUIForMode();
        fetchProfileData();

        tvUploadPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        imgAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        saveButton.setOnClickListener(v -> saveProfileData());
    }

    private void updateUIForMode() {
        if (toolbarRoleText != null) {
            toolbarRoleText.setText(isOrganizerMode ? "Organizer" : "Entrant");
        }

        // Setup Navbar dynamically based on mode
        ViewGroup navbarContainer = findViewById(R.id.navbarContainer);
        if (navbarContainer != null) {
            navbarContainer.removeAllViews();
            int layoutId = isOrganizerMode ? R.layout.layout_navbar_organizer : R.layout.layout_navbar_entrant;
            View navbarView = getLayoutInflater().inflate(layoutId, navbarContainer, false);
            // Crucial: Set the ID to R.id.navbar so NavbarEntrant/NavbarOrganizer.setup can find it.
            navbarView.setId(R.id.navbar);
            navbarContainer.addView(navbarView);
        }

        if (isOrganizerMode) {
            NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.PROFILE);
        } else {
            NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.PROFILE);
        }
    }

    private void fetchProfileData() {
        String collection = isOrganizerMode ? "organizers" : "entrants";
        db.collection(collection).document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editTextName.setText(documentSnapshot.getString("name"));
                        editTextEmail.setText(documentSnapshot.getString("email"));
                        editTextPhone.setText(documentSnapshot.getString("phone"));
                        
                        profileImageBase64 = documentSnapshot.getString("profileImage");
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            displayBase64Image(profileImageBase64);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
    }

    private void processAndSetImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap != null) {
                // Resize if too large
                int maxWidth = 500;
                int maxHeight = 500;
                if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                    float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(ratio * bitmap.getWidth()), Math.round(ratio * bitmap.getHeight()), true);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                profileImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                
                imgAvatar.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBase64Image(String base64) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null) {
                imgAvatar.setImageBitmap(bitmap);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Validates user input and updates the profile information in Firebase Firestore.
     * Upon successful update, returns the user to the ProfileActivity.
     */
    private void saveProfileData() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phoneRaw = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in Name and Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email format");
            return;
        }

        String phoneFormatted = phoneRaw;
        if (!phoneRaw.isEmpty()) {
            // Remove all non-digit characters
            String digits = phoneRaw.replaceAll("\\D", "");
            
            // Standardize to 10 digits
            if (digits.length() == 10) {
                phoneFormatted = digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
            } else if (digits.length() == 11 && digits.startsWith("1")) {
                phoneFormatted = "1-" + digits.substring(1, 4) + "-" + digits.substring(4, 7) + "-" + digits.substring(7);
            } else {
                editTextPhone.setError("Invalid phone number. Please enter a 10-digit phone number.");
                return;
            }
            editTextPhone.setText(phoneFormatted);
        }

        String collection = isOrganizerMode ? "organizers" : "entrants";
        db.collection(collection).document(deviceId)
                .update("name", name,
                        "email", email,
                        "phone", phoneFormatted,
                        "profileImage", profileImageBase64,
                        "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    intent.putExtra("isOrganizerMode", isOrganizerMode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
