package com.example.lottery_legend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

/**
 * This class is the fragment for the admin event detail view. It handles the deletion of events.
 */
public class AdminEventDetailFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId, organizerId;
    private Event currentEvent;

    public static AdminEventDetailFragment newInstance(String eventId) {
        AdminEventDetailFragment fragment = new AdminEventDetailFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_admin_event_detail, container, false);
        View toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }

        view.findViewById(R.id.btn_delete_event_admin).setOnClickListener(v -> showDeleteDialog());

        view.findViewById(R.id.organizer_profile_click).setOnClickListener(v -> {
            if (organizerId != null) {
                fetchOrganizerAndNavigate();
            }
        });

        fetchEventDetails(view);

        return view;
    }

    /**
     * Fetches the event details from the database and updates the UI.
     * @param view The view to update.
     */
    private void fetchEventDetails(View view) {
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentEvent = documentSnapshot.toObject(Event.class);
                if (currentEvent != null) {
                    organizerId = currentEvent.getOrganizerId();
                    updateUI(view);
                    fetchOrganizerName(view);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the UI with the event details.
     * @param view The view to update.
     */
    private void updateUI(View view) {
        TextView tvTitle = view.findViewById(R.id.event_title_detail);
        TextView tvDate = view.findViewById(R.id.detail_event_date);
        TextView tvDeadline = view.findViewById(R.id.detail_registration_deadline);
        TextView tvLocation = view.findViewById(R.id.detail_location);
        TextView tvCapacity = view.findViewById(R.id.detail_capacity);
        TextView tvWaitingList = view.findViewById(R.id.detail_waiting_list);
        TextView tvAbout = view.findViewById(R.id.detail_about_event);
        TextView tvGuidelines = view.findViewById(R.id.detail_lottery_guidelines);
        ImageView ivPoster = view.findViewById(R.id.event_poster_detail);

        tvTitle.setText(currentEvent.getTitle());
        String eventDateRange = currentEvent.getEventStartDate() + (currentEvent.getEventEndDate() != null ? " - " + currentEvent.getEventEndDate() : "");
        tvDate.setText(eventDateRange);
        tvDeadline.setText(currentEvent.getRegistrationEndDate());
        tvLocation.setText(currentEvent.getLocation());
        tvCapacity.setText(String.format(Locale.getDefault(), "%d Spots", currentEvent.getCapacity()));
        
        int waiting = currentEvent.getWaitingList() != null ? currentEvent.getWaitingList().size() : 0;
        if (waiting == 1) {
            tvWaitingList.setText("1 entrant registered");
        } else {
            tvWaitingList.setText(String.format(Locale.getDefault(), "%d entrants registered", waiting));
        }
        
        tvAbout.setText(currentEvent.getDescription());

        String guidelinesText = String.format(Locale.getDefault(),
                "• Random selection on %s \n• Winners notified via app\n• 48 hours to accept invitation\n• Replacements drawn if declined",
                currentEvent.getDrawDate());
        tvGuidelines.setText(guidelinesText);

        if (currentEvent.getPosterImage() != null && !currentEvent.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(currentEvent.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivPoster.setImageBitmap(bitmap);
            } catch (Exception e) {
                ivPoster.setImageResource(R.drawable.img_poster);
            }
        }
    }

    /**
     * Fetches the organizer name from the database and updates the UI.
     * @param view The view to update.
     */
    private void fetchOrganizerName(View view) {
        db.collection("organizers").document(organizerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                TextView tvOrgName = view.findViewById(R.id.detail_organizer_name);
                tvOrgName.setText(name != null ? name : "Organizer");
            }
        });
    }

    /**
     * Fetches the organizer details from the database and navigates to the organizer's profile.
     */
    private void fetchOrganizerAndNavigate() {
        db.collection("organizers").document(organizerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Organizer organizer = doc.toObject(Organizer.class);
                if (organizer != null) {
                    Intent intent = new Intent(getActivity(), AdminUserDetailActivity.class);
                    intent.putExtra("userId", organizer.getUserId());
                    intent.putExtra("name", organizer.getName());
                    intent.putExtra("email", organizer.getEmail());
                    intent.putExtra("phone", organizer.getPhone());
                    intent.putExtra("collectionName", "organizers");
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Shows a dialog to confirm the deletion of the event.
     * If the admin confirms, the event is removed from the database.
     */
    private void showDeleteDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_admin_delete, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView)
                .create();

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);

        title.setText("Delete Event");
        message.setText("This will permanently remove this event.");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            db.collection("events").document(eventId).delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Event removed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                getParentFragmentManager().popBackStack();
            });
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}
