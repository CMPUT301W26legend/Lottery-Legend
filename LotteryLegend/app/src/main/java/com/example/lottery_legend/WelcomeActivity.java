package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is the welcome screen of the app.
 * It handles the flow for both new users and existing users.
 */
public class WelcomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        checkUserStatus();
    }

    private void checkUserStatus() {
        db.collection("entrants").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setupExistingUserUI();
                    } else {
                        // If entrant missing, check if organizer exists
                        db.collection("organizers").document(deviceId).get()
                                .addOnSuccessListener(organizerDoc -> {
                                    if (organizerDoc.exists()) {
                                        setupExistingUserUI();
                                    } else {
                                        setupNewUserUI();
                                    }
                                })
                                .addOnFailureListener(e -> setupNewUserUI());
                    }
                })
                .addOnFailureListener(e -> setupNewUserUI());
    }

    private void setupNewUserUI() {
        setContentView(R.layout.activity_welcome);
        applyWindowInsets(findViewById(R.id.main));

        Button createProfileButton = findViewById(R.id.CreateProfileButton);
        if (createProfileButton != null) {
            createProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateProfileActivity.class);
                intent.putExtra("deviceID", deviceId);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupExistingUserUI() {
        setContentView(R.layout.activity_welcome_exist);
        applyWindowInsets(findViewById(R.id.main));

        View enterAppButton = findViewById(R.id.btnContinue);
        View editProfileRow = findViewById(R.id.editProfileRow);
        View btnInvitations = findViewById(R.id.btnInvitations);
        View btnViewEvents = findViewById(R.id.btnViewEvents);

        if (enterAppButton != null) {
            enterAppButton.setOnClickListener(v -> {
                // If entrant profile exists, go to MainActivity, otherwise go to OrganizerMain
                db.collection("entrants").document(deviceId).get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, OrganizerMainActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                    }
                    finish();
                });
            });
        }

        if (editProfileRow != null) {
            editProfileRow.setOnClickListener(v -> {
                // Determine which profile mode to open based on available data
                db.collection("entrants").document(deviceId).get().addOnSuccessListener(doc -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    if (!doc.exists()) {
                        intent.putExtra("isOrganizerMode", true);
                    }
                    startActivity(intent);
                });
            });
        }

        if (btnInvitations != null) {
            btnInvitations.setOnClickListener(v -> {
                // Future invitations implementation
            });
        }

        if (btnViewEvents != null) {
            btnViewEvents.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * Applies system bar insets as padding to the root view.
     * This pushes the entire layout (including the blue toolbar) down so it is not 
     * covered by the status bar/camera cutout, keeping the area above the toolbar 
     * as the system default (usually white/transparent).
     */
    private void applyWindowInsets(View rootView) {
        if (rootView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.requestApplyInsets(rootView);
    }
}
