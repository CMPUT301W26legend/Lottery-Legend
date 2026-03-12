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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class WaitingListDialogFragment extends DialogFragment {

    private Event event;
    private String deviceId;

    public static WaitingListDialogFragment newInstance(Event event, String deviceId) {
        WaitingListDialogFragment fragment = new WaitingListDialogFragment();
        fragment.event = event;
        fragment.deviceId = deviceId;
        return fragment;
    }

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
        locationText.setText(event.getLocation());
        deadlineText.setText(event.getRegistrationEndDate());
        waitingCountText.setText(String.valueOf(event.getWaitingList().size()));

        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    posterImage.setImageBitmap(bitmap);
                    posterImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            } catch (Exception e) {

            }
        }

        if (!event.isGeoEnabled()) {
            View locationRow = view.findViewById(R.id.locationRow);
            if (locationRow != null) {
                locationRow.setVisibility(View.GONE);
            }
        }

        boolean isJoined = event.getWaitingList().contains(deviceId);
        if (isJoined) {
            if (dialogHeader != null) dialogHeader.setText("Leave Waiting List");
            if (dialogPrompt != null) dialogPrompt.setText("Do you want to leave this waiting list?");
            btnConfirm.setText("Leave");
            btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));
        }

        btnCancel.setOnClickListener(v -> dismiss());
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event == null || deviceId == null) return;

                boolean isJoined = event.getWaitingList().contains(deviceId);
                if (isJoined) {
                    FirebaseFirestore.getInstance().collection("events").document(event.getEventId())
                            .update("waitingList", FieldValue.arrayRemove(deviceId))
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            });
                } else {
                    FirebaseFirestore.getInstance().collection("events").document(event.getEventId())
                            .update("waitingList", FieldValue.arrayUnion(deviceId))
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
