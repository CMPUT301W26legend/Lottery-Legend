package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.lottery_legend.R;
import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Organizer;
import com.example.lottery_legend.organizer.NavbarOrganizer;
import com.example.lottery_legend.organizer.OrganizerMainActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;

/**
 * Activity that displays and manages the user's profile information.
 * It supports both Entrant and Organizer modes, allowing users to switch between roles,
 * edit their details, manage notification preferences, and delete their accounts.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    
    private TextView viewName;
    private TextView viewEmail;
    private TextView viewPhone;
    private ImageView imgAvatar;
    private TextView toolbarRoleText;
    private TextView switchRoleLabel;
    private SwitchMaterial switchNotifications;
    private LinearLayout layoutSwitchRole;
    private LinearLayout layoutNotifications;
    private LinearLayout layoutGoToNotifications;
    private Button buttonEditProfile;
    private Button btnContinueAsAdmin;
    private Button btnDeleteAccount;
    private MaterialToolbar toolbar;

    private Entrant currentEntrant;
    private boolean isOrganizerMode = false;
    private boolean isReadOnly = false;

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
        isOrganizerMode = getIntent().getBooleanExtra("isOrganizerMode", false);
        isReadOnly = getIntent().getBooleanExtra("isReadOnly", false);

        initViews();

        updateUIForMode();

        if (isReadOnly) {
            setupReadOnlyMode();
        } else {
            setupFullMode();
        }

        refreshProfile();
    }

    /**
     * Initializes UI references for all views in the activity.
     */
    private void initViews() {
        toolbar = findViewById(R.id.topToolbar);
        viewName = findViewById(R.id.viewName);
        viewEmail = findViewById(R.id.viewEmail);
        viewPhone = findViewById(R.id.viewPhone);
        imgAvatar = findViewById(R.id.imgAvatar);
        switchNotifications = findViewById(R.id.switchNotifications);
        layoutSwitchRole = findViewById(R.id.layoutSwitchRole);
        layoutNotifications = findViewById(R.id.layoutNotifications);
        layoutGoToNotifications = findViewById(R.id.layoutGoToNotifications);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        btnContinueAsAdmin = findViewById(R.id.btnContinueAsAdmin);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        toolbarRoleText = findViewById(R.id.toolbarRoleText);
        switchRoleLabel = findViewById(R.id.switchRoleLabel);
    }

    /**
     * Configures the UI for read-only mode, hiding management buttons and navigation.
     */
    private void setupReadOnlyMode() {
        buttonEditProfile.setVisibility(View.GONE);
        layoutSwitchRole.setVisibility(View.GONE);
        btnDeleteAccount.setVisibility(View.GONE);
        btnContinueAsAdmin.setVisibility(View.GONE);
        layoutNotifications.setVisibility(View.GONE);
        layoutGoToNotifications.setVisibility(View.GONE);
        
        // Hide navigation bar in read-only mode
        View navbarContainer = findViewById(R.id.navbar);
        if (navbarContainer != null) {
            navbarContainer.setVisibility(View.GONE);
        }
        
        // Add back button navigation to the toolbar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Configures interactive elements for full-access mode.
     */
    private void setupFullMode() {
        btnContinueAsAdmin.setVisibility(View.GONE);

        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("deviceId", deviceId);
            intent.putExtra("isOrganizerMode", isOrganizerMode);
            startActivity(intent);
        });

        btnContinueAsAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());

        layoutGoToNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });

        layoutSwitchRole.setOnClickListener(v -> {
            if (isOrganizerMode) {
                switchToEntrantMode();
            } else {
                checkAndCreateOrganizerAccount();
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                updateNotificationPreference(isChecked);
            }
        });
    }

    /**
     * Updates the user's notification enablement status in Firestore.
     */
    private void updateNotificationPreference(boolean isEnabled) {
        if (deviceId == null) return;

        db.collection("entrants").document(deviceId)
                .update("notificationsEnabled", isEnabled)
                .addOnSuccessListener(aVoid -> {
                    if (currentEntrant != null) {
                        currentEntrant.setNotificationsEnabled(isEnabled);
                    }
                    String status = isEnabled ? "enabled" : "disabled";
                    Toast.makeText(ProfileActivity.this, "Notifications " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    switchNotifications.setChecked(!isEnabled);
                    Toast.makeText(ProfileActivity.this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Refreshes the profile data based on the current mode (Entrant vs Organizer).
     */
    private void refreshProfile() {
        if (isOrganizerMode) {
            fetchOrganizerData();
        } else {
            fetchEntrantData();
        }
        
        if (!isReadOnly) {
            setupNavbar();
        }
    }

    /**
     * Updates text labels and visibility based on whether the user is in Entrant or Organizer mode.
     */
    private void updateUIForMode() {
        if (toolbarRoleText != null) {
            toolbarRoleText.setText(isOrganizerMode ? "Organizer" : "Entrant");
        }
        if (switchRoleLabel != null) {
            switchRoleLabel.setText(isOrganizerMode ? "Switch to Entrant Mode" : "Switch to Organizer Mode");
        }
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setText(isOrganizerMode ? "Delete Organizer Account" : "Delete Entrant Account");
        }
        
        if (!isReadOnly) {
            if (layoutNotifications != null) {
                layoutNotifications.setVisibility(isOrganizerMode ? View.GONE : View.VISIBLE);
            }
            if (layoutGoToNotifications != null) {
                layoutGoToNotifications.setVisibility(isOrganizerMode ? View.GONE : View.VISIBLE);
            }
        }
        
        // Dynamically update the persistent bottom navigation bar
        if (!isReadOnly) {
            View navbarContainer = findViewById(R.id.navbar);
            if (navbarContainer instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) navbarContainer;
                group.removeAllViews();
                int layoutId = isOrganizerMode ? R.layout.layout_navbar_organizer : R.layout.layout_navbar_entrant;
                getLayoutInflater().inflate(layoutId, group, true);
            }
        }
    }

    /**
     * Fetches entrant profile data from Firestore and updates the UI.
     */
    private void fetchEntrantData() {
        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEntrant = documentSnapshot.toObject(Entrant.class);
                        if (currentEntrant != null) {
                            viewName.setText(currentEntrant.getName());
                            viewEmail.setText(currentEntrant.getEmail());

                            if (currentEntrant.getPhone() != null && !currentEntrant.getPhone().isEmpty()) {
                                viewPhone.setText(currentEntrant.getPhone());
                            } else {
                                viewPhone.setText("No phone number provided");
                            }

                            switchNotifications.setChecked(currentEntrant.isNotificationsEnabled());
                            
                            String profileImage = currentEntrant.getProfileImage();
                            if (profileImage != null && !profileImage.isEmpty()) {
                                displayBase64Image(profileImage);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_profile_avatar);
                            }

                            // Verify admin privileges
                            if (!isReadOnly && documentSnapshot.getBoolean("isAdmin") != null && documentSnapshot.getBoolean("isAdmin")) {
                                btnContinueAsAdmin.setVisibility(View.VISIBLE);
                            } else {
                                btnContinueAsAdmin.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Log.d("ProfileActivity", "No entrant document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching entrant document", e);
                    Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches organizer profile data from Firestore and updates the UI.
     */
    private void fetchOrganizerData() {
        db.collection("organizers").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                viewName.setText(doc.getString("name"));
                viewEmail.setText(doc.getString("email"));
                String phone = doc.getString("phone");
                viewPhone.setText((phone != null && !phone.isEmpty()) ? phone : "No phone number provided");
                
                String profileImage = doc.getString("profileImage");
                if (profileImage != null && !profileImage.isEmpty()) {
                    displayBase64Image(profileImage);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_profile_avatar);
                }
                
                if (!isReadOnly) {
                    // Admins are identified via the entrants collection
                    db.collection("entrants").document(deviceId).get().addOnSuccessListener(entrantDoc -> {
                        if (entrantDoc.exists() && entrantDoc.getBoolean("isAdmin") != null && entrantDoc.getBoolean("isAdmin")) {
                            btnContinueAsAdmin.setVisibility(View.VISIBLE);
                        } else {
                            btnContinueAsAdmin.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    /**
     * Helper method to decode and display a Base64 encoded profile image.
     */
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
     * Displays a confirmation dialog before proceeding with account deletion.
     */
    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_profile_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView title = dialogView.findViewById(R.id.textDeleteTitle);
        TextView message = dialogView.findViewById(R.id.textDeleteMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelDelete);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmDelete);

        String titleStr = isOrganizerMode ? "Delete Organizer Account" : "Delete Entrant Account";
        String messageStr = isOrganizerMode ? 
                "Are you sure you want to delete your Organizer profile? Your Entrant profile (if any) will remain." :
                "Are you sure you want to delete your Entrant profile? Your Organizer profile (if any) will remain.";

        title.setText(titleStr);
        message.setText(messageStr);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (isOrganizerMode) {
                deleteOrganizerAccount();
            } else {
                deleteEntrantAccount();
            }
        });

        dialog.show();
    }

    /**
     * Deletes the user's entrant record and removes them from all event waiting lists.
     */
    private void deleteEntrantAccount() {
        WriteBatch batch = db.batch();

        db.collection("entrants").document(deviceId).collection("notifications").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.delete(db.collection("entrants").document(deviceId));

                    db.collection("events")
                            .whereArrayContains("waitingList", deviceId)
                            .get()
                            .addOnSuccessListener(events -> {
                                for (QueryDocumentSnapshot document : events) {
                                    batch.update(document.getReference(), "waitingList", FieldValue.arrayRemove(deviceId));
                                }

                                batch.commit().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(), "Entrant profile deleted", Toast.LENGTH_SHORT).show();
                                    checkRemainingAccountAfterEntrantDelete();
                                }).addOnFailureListener(e -> {
                                    Log.e("ProfileActivity", "Error deleting account", e);
                                });
                            })
                            .addOnFailureListener(e -> {
                                batch.commit().addOnSuccessListener(aVoid -> {
                                    checkRemainingAccountAfterEntrantDelete();
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    db.collection("entrants").document(deviceId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "Entrant profile deleted", Toast.LENGTH_SHORT).show();
                                checkRemainingAccountAfterEntrantDelete();
                            });
                });
    }

    /**
     * Deletes the user's organizer record and all their associated created events.
     */
    private void deleteOrganizerAccount() {
        WriteBatch batch = db.batch();
        
        db.collection("organizers").document(deviceId).collection("createdEvents").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    
                    batch.delete(db.collection("organizers").document(deviceId));
                    
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Organizer profile deleted", Toast.LENGTH_SHORT).show();
                        checkRemainingAccountAfterOrganizerDelete();
                    }).addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Error deleting organizer account", e);
                        Toast.makeText(ProfileActivity.this, "Error deleting organizer account", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    db.collection("organizers").document(deviceId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "Organizer profile deleted", Toast.LENGTH_SHORT).show();
                                checkRemainingAccountAfterOrganizerDelete();
                            });
                });
    }

    /**
     * Checks if the user still has an organizer account after deleting their entrant account.
     */
    private void checkRemainingAccountAfterEntrantDelete() {
        db.collection("organizers").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                isOrganizerMode = true;
                updateUIForMode();
                refreshProfile();
            } else {
                closeApp();
            }
        });
    }

    /**
     * Checks if the user still has an entrant account after deleting their organizer account.
     */
    private void checkRemainingAccountAfterOrganizerDelete() {
        db.collection("entrants").document(deviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                isOrganizerMode = false;
                updateUIForMode();
                refreshProfile();
            } else {
                closeApp();
            }
        });
    }

    /**
     * Terminates the application if no profiles remain.
     */
    private void closeApp() {
        finishAffinity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        }
    }

    /**
     * Checks if an organizer account exists; creates one using entrant data if it doesn't.
     */
    private void checkAndCreateOrganizerAccount() {
        db.collection("organizers").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    isOrganizerMode = true;
                    updateUIForMode();
                    refreshProfile();
                } else {
                    if (currentEntrant != null) {
                        Timestamp now = Timestamp.now();
                        Organizer newOrganizer = new Organizer(
                                deviceId,
                                currentEntrant.getName(),
                                currentEntrant.getEmail(),
                                currentEntrant.getPhone(),
                                currentEntrant.getJoinDate(),
                                now,
                                currentEntrant.getIsAdmin(),
                                new ArrayList<>()
                        );
                        db.collection("organizers").document(deviceId).set(newOrganizer)
                                .addOnSuccessListener(aVoid -> {
                                    isOrganizerMode = true;
                                    updateUIForMode();
                                    refreshProfile();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfileActivity.this, "Failed to create organizer profile", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
        });
    }

    /**
     * Toggles role back to Entrant mode.
     */
    private void switchToEntrantMode() {
        isOrganizerMode = false;
        updateUIForMode();
        refreshProfile();
    }

    /**
     * Initializes the role-specific navigation bar.
     */
    private void setupNavbar() {
        if (isOrganizerMode) {
            NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.PROFILE);
        } else {
            NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.PROFILE);
        }
    }
}
