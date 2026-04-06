package com.example.lottery_legend.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.lottery_legend.R;
import com.example.lottery_legend.entrant.MainActivity;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.organizer.OrganizerMainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

/**
 * Fragment that displays the Administrator's profile information and provides options
 * to switch between user modes or delete the account.
 *
 * <p>Role: This fragment serves as the 'Administrative Account Manager' for the Admin subsystem.
 * It utilizes the State Switcher design pattern to toggle between different app modes (Admin, Entrant, Organizer).</p>
 *
 * <p>Switching modes performs a full task reset; users may see a brief loading state while Firestore re-syncs their data.</p>
 */
public class AdminProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private String deviceId;
    private TextView viewName, viewEmail, viewPhone;
    private ImageView profileAvatar;
    private LinearLayout layoutSwitchToEntrant, layoutSwitchToOrganizer;
    private Button btnDeleteAccount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        
        // Retrieve deviceId from arguments or fallback to ANDROID_ID
        if (getArguments() != null) {
            deviceId = getArguments().getString("deviceId");
        }
        
        // Fallback if not in arguments
        if (deviceId == null && getActivity() != null && getActivity().getIntent() != null) {
            deviceId = getActivity().getIntent().getStringExtra("deviceId");
        }
        
        // Final fallback to ANDROID_ID
        if (deviceId == null && getContext() != null) {
            deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        viewName = view.findViewById(R.id.admin_viewName);
        viewEmail = view.findViewById(R.id.admin_viewEmail);
        viewPhone = view.findViewById(R.id.admin_viewPhone);
        profileAvatar = view.findViewById(R.id.admin_profile_avatar);
        layoutSwitchToEntrant = view.findViewById(R.id.layoutSwitchToEntrant);
        layoutSwitchToOrganizer = view.findViewById(R.id.layoutSwitchToOrganizer);
        btnDeleteAccount = view.findViewById(R.id.admin_btnDeleteAccount);

        fetchAdminData();

        layoutSwitchToEntrant.setOnClickListener(v -> {
            String idToPass = deviceId;
            if (idToPass == null && getContext() != null) {
                idToPass = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.putExtra("deviceId", idToPass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        layoutSwitchToOrganizer.setOnClickListener(v -> {
            String idToPass = deviceId;
            if (idToPass == null && getContext() != null) {
                idToPass = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            Intent intent = new Intent(requireActivity(), OrganizerMainActivity.class);
            intent.putExtra("deviceId", idToPass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());

        return view;
    }

    /**
     * Fetches administrator profile data from the "entrants" collection in Firestore.
     */
    private void fetchAdminData() {
        if (deviceId == null) return;

        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant admin = documentSnapshot.toObject(Entrant.class);
                        if (admin != null) {
                            admin_viewName_update(admin);
                        }
                    } else {
                        Log.d("AdminProfileFragment", "Admin profile not found for ID: " + deviceId);
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminProfileFragment", "Error fetching admin data", e));
    }

    /**
     * Updates UI components with fetched Entrant data.
     * @param admin The Entrant object containing admin profile data.
     */
    private void admin_viewName_update(Entrant admin) {
        viewName.setText(admin.getName());
        viewEmail.setText(admin.getEmail());
        viewPhone.setText(admin.getPhone() != null && !admin.getPhone().isEmpty() ? admin.getPhone() : "No phone provided");
        
        String profileImage = admin.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            displayBase64Image(profileImage);
        } else {
            profileAvatar.setImageResource(R.drawable.ic_profile_avatar);
        }
    }

    /**
     * Helper method to decode Base64 string into a Bitmap and set it to the ImageView.
     * @param base64 The Base64 encoded image string.
     */
    private void displayBase64Image(String base64) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null) {
                profileAvatar.setImageBitmap(bitmap);
            } else {
                profileAvatar.setImageResource(R.drawable.ic_profile_avatar);
            }
        } catch (Exception e) {
            profileAvatar.setImageResource(R.drawable.ic_profile_avatar);
        }
    }

    /**
     * Displays a confirmation dialog before deleting the account.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently remove your administrator account. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteAccountWithSubcollections();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccountWithSubcollections() {
        if (deviceId == null) return;
        
        WriteBatch batch = db.batch();
        db.collection("entrants").document(deviceId).collection("notifications").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(db.collection("entrants").document(deviceId));
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().finishAffinity();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    db.collection("entrants").document(deviceId).delete().addOnSuccessListener(aVoid -> {
                        if (getActivity() != null) {
                            getActivity().finishAffinity();
                        }
                    });
                });
    }
}
