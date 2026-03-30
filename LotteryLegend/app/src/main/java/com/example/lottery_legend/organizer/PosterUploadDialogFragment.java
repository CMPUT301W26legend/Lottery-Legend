package com.example.lottery_legend.organizer;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lottery_legend.R;

/**
 * A DialogFragment that for organizers to upload or remove an event poster image.
 * It uses the system's photo picker to select an image and provides callbacks to the parent activity.
 */
public class PosterUploadDialogFragment extends DialogFragment {

    /**
     * Interface definition for callbacks to be invoked when a poster event occurs.
     */
    public interface OnPosterEventListener {
        /**
         * Called when a new poster image has been selected.
         * @param uri The Uri of the selected image.
         */
        void onPosterSelected(Uri uri);

        /**
         * Called when the current poster image has been removed.
         */
        void onPosterRemoved();
    }

    private OnPosterEventListener listener;
    private Uri currentUri;
    private ImageView imagePreview;
    private ImageView btnDelete;

    /**
     * Activity result launcher for picking an image from the device's storage.
     * Uses the GetContent contract to filter for images.
     */
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    updatePreview(uri);
                }
            }
    );

    /**
     * Sets the listener for poster events.
     * @param listener The listener to be notified.
     */
    public void setOnPosterEventListener(OnPosterEventListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the current poster Uri to be displayed when the dialog opens.
     * @param uri The Uri of the existing poster.
     */
    public void setCurrentUri(Uri uri) {
        this.currentUri = uri;
    }

    /**
     * Inflates the dialog layout and initializes UI components and listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_poster_dialog, container, false);

        imagePreview = view.findViewById(R.id.imagePosterPreview);
        btnDelete = view.findViewById(R.id.btnDeletePoster);
        Button uploadButton = view.findViewById(R.id.btnUploadConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Pre-populate preview if a Uri is already provided
        if (currentUri != null) {
            updatePreview(currentUri);
        }

        // Clicking the preview area launches the image picker
        imagePreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Delete button removes the poster locally
        btnDelete.setOnClickListener(v -> removePosterLocally());

        // Cancel button dismisses the dialog without changes
        btnCancel.setOnClickListener(v -> dismiss());

        // Confirm button notifies the listener and dismisses the dialog
        uploadButton.setOnClickListener(v -> {
            if (currentUri != null && listener != null) {
                listener.onPosterSelected(currentUri);
            }
            dismiss();
        });

        return view;
    }

    /**
     * Resets the local poster state, clearing the preview and notifying the listener.
     */
    private void removePosterLocally() {
        currentUri = null;
        // Reset to placeholder image
        imagePreview.setImageResource(R.drawable.img_poster);
        imagePreview.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btnDelete.setVisibility(View.GONE);
        if (listener != null) listener.onPosterRemoved();
    }

    /**
     * Updates the UI preview with the selected image Uri.
     * @param uri The Uri of the image to display.
     */
    private void updatePreview(Uri uri) {
        currentUri = uri;
        imagePreview.setImageURI(uri);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btnDelete.setVisibility(View.VISIBLE);
    }

    /**
     * Configures the dialog's window properties, such as background transparency and width.
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
