package com.example.lottery_legend;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * A DialogFragment that allows users to join or leave the waiting list for a specific event.
 */
public class WaitingListDialogFragment extends DialogFragment {

    private Event event;
    private String deviceId;

    /**
     * Creates a new instance of WaitingListDialogFragment with the provided event and device ID.
     *
     * @param event    The Event object to be displayed and managed.
     * @param deviceId The device ID of the current user.
     * @return A new instance of WaitingListDialogFragment.
     */
    public static WaitingListDialogFragment newInstance(Event event, String deviceId) {
        WaitingListDialogFragment fragment = new WaitingListDialogFragment();
        fragment.event = event;
        fragment.deviceId = deviceId;
        return fragment;
    }

    /**
     * Inflates the dialog layout, initializes UI components with event data,
     * and sets up confirm/cancel button listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waiting_list_dialog, container, false);

        ImageView posterImage = view.findViewById(R.id.posterImage);
        TextView titleText = view.findViewById(R.id.title);
        TextView locationText = view.findViewById(R.id.locationText);
        TextView deadlineText = view.findViewById(R.id.deadline);
        TextView waitingCountText = view.findViewById(R.id.waitingCount);
        TextView dialogHeader = view.findViewById(R.id.dialogHeader);
        TextView dialogPrompt = view.findViewById(R.id.dialogPrompt);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        titleText.setText(event.getTitle());
        
        Event.EventLocation loc = event.getEventLocation();
        locationText.setText(loc != null ? loc.getName() : "");
        
        Timestamp regEndAt = event.getRegistrationEndAt();
        if (regEndAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
            deadlineText.setText(sdf.format(regEndAt.toDate()));
        } else {
            deadlineText.setText("");
        }

        int waitingListSize = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        waitingCountText.setText(String.valueOf(waitingListSize));

        // Decode and set the poster image if it exists
        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    posterImage.setImageBitmap(bitmap);
                    posterImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            } catch (Exception e) {
                // Ignore decoding errors
            }
        }

        // Hide location row if geolocation is not enabled for the event
        if (!event.isGeoEnabled()) {
            View locationRow = view.findViewById(R.id.locationRow);
            if (locationRow != null) {
                locationRow.setVisibility(View.GONE);
            }
        }

        // Check if the user is already on the waiting list to customize dialog text
        boolean isJoined = false;
        if (event.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : event.getWaitingList()) {
                if (Objects.equals(entry.getDeviceId(), deviceId)) {
                    isJoined = true;
                    break;
                }
            }
        }

        if (isJoined) {
            if (dialogHeader != null) dialogHeader.setText("Leave Waiting List");
            if (dialogPrompt != null) dialogPrompt.setText("Do you want to leave this waiting list?");
            btnConfirm.setText("Leave");
            btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));
        }

        // Cancel button dismisses the dialog
        btnCancel.setOnClickListener(v -> dismiss());

        // Confirm button handles joining or leaving based on current state
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event == null || deviceId == null) return;

                boolean currentlyJoined = false;
                Event.WaitingListEntry existingEntry = null;
                if (event.getWaitingList() != null) {
                    for (Event.WaitingListEntry entry : event.getWaitingList()) {
                        if (Objects.equals(entry.getDeviceId(), deviceId)) {
                            currentlyJoined = true;
                            existingEntry = entry;
                            break;
                        }
                    }
                }

                if (currentlyJoined) {
                    // Remove user from waitingList in Firestore
                    FirebaseFirestore.getInstance().collection("events").document(event.getEventId())
                            .update("waitingList", FieldValue.arrayRemove(existingEntry))
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            });
                } else {
                    // Add user to waitingList in Firestore
                    Event.WaitingListEntry newEntry = new Event.WaitingListEntry();
                    newEntry.setDeviceId(deviceId);
                    newEntry.setJoinedAt(Timestamp.now());
                    newEntry.setUpdatedAt(Timestamp.now());
                    newEntry.setParticipationStatus("waiting");
                    
                    FirebaseFirestore.getInstance().collection("events").document(event.getEventId())
                            .update("waitingList", FieldValue.arrayUnion(newEntry))
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Joined waiting list!", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            });
                }
            }
        });

        return view;
    }

    /**
     * Configures the dialog's window properties, such as a transparent background and custom width.
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
