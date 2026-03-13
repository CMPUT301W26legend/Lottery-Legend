package com.example.lottery_legend;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

/**
 * Activity that displays and manages the user's profile information.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    
    private TextView viewName;
    private TextView viewEmail;
    private TextView viewPhone;
    private SwitchMaterial switchNotifications;
    private LinearLayout layoutSwitchOrganizer;
    private Button buttonEditProfile;
    private Button btnContinueAsAdmin;
    private Button btnDeleteAccount;

    private Entrant currentEntrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        viewName = findViewById(R.id.viewName);
        viewEmail = findViewById(R.id.viewEmail);
        viewPhone = findViewById(R.id.viewPhone);
        switchNotifications = findViewById(R.id.switchNotifications);
        layoutSwitchOrganizer = findViewById(R.id.layoutSwitchOrganizer);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        btnContinueAsAdmin = findViewById(R.id.btnContinueAsAdmin);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Initially hide the admin button until permissions are verified
        btnContinueAsAdmin.setVisibility(View.GONE);

        // Setup click listener for editing the profile
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });

        // Setup click listener for the admin panel
        btnContinueAsAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        // Fetch user data and initialize the navigation bar
        fetchProfileData();
        setupNavbar();

        // Setup click listener for switching to organizer mode
        layoutSwitchOrganizer.setOnClickListener(v -> {
            checkAndCreateOrganizerAccount();
        });
    }

    /**
     * Fetches the user's profile data from Firestore using the device ID.
     * Updates the UI with the retrieved information and checks for admin status.
     */
    private void fetchProfileData() {
        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEntrant = documentSnapshot.toObject(Entrant.class);
                        viewName.setText(currentEntrant.name);
                        viewEmail.setText(currentEntrant.email);

                        if (currentEntrant.phone != null && !currentEntrant.phone.isEmpty()) {
                            viewPhone.setText(currentEntrant.phone);
                        } else {
                            viewPhone.setText("No phone number provided");
                        }

                        switchNotifications.setChecked(currentEntrant.notification);

                        // Show admin button if the user has admin privileges
                        if (documentSnapshot.getBoolean("isAdmin") != null && documentSnapshot.getBoolean("isAdmin")) {
                            btnContinueAsAdmin.setVisibility(View.VISIBLE);
                        } else {
                            btnContinueAsAdmin.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d("ProfileActivity", "No such document");
                        Toast.makeText(ProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching document", e);
                    Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone and the app will close.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        WriteBatch batch = db.batch();

        // 1. Delete from entrants collection
        batch.delete(db.collection("entrants").document(deviceId));

        // 2. Delete from organizers collection
        batch.delete(db.collection("organizers").document(deviceId));

        // 3. Remove from all waiting lists
        db.collection("events")
                .whereArrayContains("waitingList", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.update(document.getReference(), "waitingList", FieldValue.arrayRemove(deviceId));
                    }

                    // Commit the batch after finding all events the user is in
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        closeApp();
                    }).addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Error deleting account", e);
                        Toast.makeText(getApplicationContext(), "Error deleting account", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Even if we fail to remove from waiting lists, we should still try to delete the profile
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Account partially deleted", Toast.LENGTH_SHORT).show();
                        closeApp();
                    });
                });
    }

    private void closeApp() {
        finishAffinity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        }
    }

    /**
     * Checks if the current user already has an organizer account.
     * If they do, it navigates to the Organizer main screen.
     * If not, it creates a new organizer account using the entrant profile data.
     */
    private void checkAndCreateOrganizerAccount() {
        db.collection("organizers").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Organizer account exists, just navigate
                    navigateToOrganizerMain();
                } else {
                    // Organizer account doesn't exist, create it from profile data
                    if (currentEntrant != null) {
                        Organizer newOrganizer = new Organizer(
                                currentEntrant.name,
                                currentEntrant.email,
                                currentEntrant.phone,
                                deviceId,
                                currentEntrant.joinDate,
                                currentEntrant.isAdmin);
                        db.collection("organizers").document(deviceId).set(newOrganizer)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProfileActivity.this, "Organizer account created", Toast.LENGTH_SHORT).show();
                                    navigateToOrganizerMain();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfileActivity.this, "Failed to create organizer account", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Please wait for profile to load", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Error checking organizer status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigates to the OrganizerMainActivity and finishes the current activity.
     */
    private void navigateToOrganizerMain() {
        Intent intent = new Intent(ProfileActivity.this, OrganizerMainActivity.class);
        intent.putExtra("deviceId", deviceId);
        startActivity(intent);
        finish();
    }

    /**
     * Configures the bottom navigation bar.
     * Highlights the profile tab and sets up the click listener for the home tab.
     */
    private void setupNavbar() {
        View navbar = findViewById(R.id.navbar);
        if (navbar != null) {
            ImageView imageProfile = navbar.findViewById(R.id.imageNavProfile);
            TextView textProfile = navbar.findViewById(R.id.textNavProfile);
            imageProfile.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2563EB")));
            textProfile.setTextColor(android.graphics.Color.parseColor("#2563EB"));

            View homeItem = navbar.findViewById(R.id.navHome);
            homeItem.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
                finish();
            });
        }
    }
}
