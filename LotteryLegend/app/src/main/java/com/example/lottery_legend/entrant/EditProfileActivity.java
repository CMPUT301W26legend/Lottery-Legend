package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.organizer.NavbarOrganizer;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private Button saveButton;
    private TextView toolbarRoleText;

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
        saveButton = findViewById(R.id.btnSave);
        toolbarRoleText = findViewById(R.id.toolbarRoleText);

        updateUIForMode();
        fetchProfileData();

        saveButton.setOnClickListener(v -> saveProfileData());
    }

    private void updateUIForMode() {
        if (toolbarRoleText != null) {
            toolbarRoleText.setText(isOrganizerMode ? "Organizer" : "Entrant");
        }

        // Setup Navbar
        View navbarContainer = findViewById(R.id.navbarContainer);
        if (navbarContainer instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) navbarContainer;
            group.removeAllViews();
            int layoutId = isOrganizerMode ? R.layout.layout_navbar_organizer : R.layout.layout_navbar_entrant;
            getLayoutInflater().inflate(layoutId, group, true);
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
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
    }

    /**
     * Validates user input and updates the profile information in Firebase Firestore.
     * Upon successful update, returns the user to the ProfileActivity.
     */
    private void saveProfileData() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in Name and Email", Toast.LENGTH_SHORT).show();
            return;
        }

        String collection = isOrganizerMode ? "organizers" : "entrants";
        db.collection(collection).document(deviceId)
                .update("name", name,
                        "email", email,
                        "phone", phone,
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
