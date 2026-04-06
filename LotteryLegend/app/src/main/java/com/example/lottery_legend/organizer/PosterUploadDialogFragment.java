package com.example.lottery_legend.organizer;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lottery_legend.R;

/**
 * A DialogFragment that provides an interface for organizers to upload, update, or remove
 * an event poster image. Supports both URI-based and Base64-based image previews.
 */
public class PosterUploadDialogFragment extends DialogFragment {

    /**
     * Interface for communicating poster-related events back to the host activity or fragment.
     */
    public interface OnPosterEventListener {
        /**
         * Called when a new poster image has been selected from the device.
         * @param uri The URI of the selected image.
         */
        void onPosterSelected(Uri uri);

        /**
         * Called when the current poster image has been removed.
         */
        void onPosterRemoved();
    }

    private OnPosterEventListener listener;
    private Uri currentUri;
    private String currentBase64;
    private ImageView imagePreview;
    private ImageView btnDelete;
    private TextView textTitle;
    private TextView textDescription;
    private Button uploadButton;

    /**
     * Launcher for the system image picker.
     */
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentBase64 = null; // Clear base64 if a new URI is selected
                    updatePreview(uri);
                    updateUITexts(true);
                }
            }
    );

    /**
     * Sets the listener for poster events.
     * @param listener The implementation of OnPosterEventListener.
     */
    public void setOnPosterEventListener(OnPosterEventListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the initial image URI for the preview.
     * @param uri The image URI.
     */
    public void setCurrentUri(Uri uri) {
        this.currentUri = uri;
    }

    /**
     * Sets the initial Base64 string for the preview.
     * @param base64 The Base64 encoded image.
     */
    public void setCurrentBase64(String base64) {
        this.currentBase64 = base64;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_poster_dialog, container, false);

        imagePreview = view.findViewById(R.id.imagePosterPreview);
        btnDelete = view.findViewById(R.id.btnDeletePoster);
        uploadButton = view.findViewById(R.id.btnUploadConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        textTitle = view.findViewById(R.id.textPosterTitle);
        textDescription = view.findViewById(R.id.textPosterDescription);

        // Pre-populate preview if data is provided
        if (currentBase64 != null && !currentBase64.isEmpty()) {
            displayBase64Image(currentBase64);
            updateUITexts(true);
        } else if (currentUri != null) {
            updatePreview(currentUri);
            updateUITexts(true);
        } else {
            updateUITexts(false);
        }

        // Clicking the preview triggers the image picker
        imagePreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnDelete.setOnClickListener(v -> removePosterLocally());
        btnCancel.setOnClickListener(v -> dismiss());

        uploadButton.setOnClickListener(v -> {
            if (currentUri != null && listener != null) {
                listener.onPosterSelected(currentUri);
            }
            dismiss();
        });

        return view;
    }

    /**
     * Decodes a Base64 string and updates the preview ImageView.
     * @param base64 The Base64 encoded image.
     */
    private void displayBase64Image(String base64) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null) {
                imagePreview.setImageBitmap(bitmap);
                imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                btnDelete.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            imagePreview.setImageResource(R.drawable.img_poster);
        }
    }

    /**
     * Updates the dialog's text labels based on whether an image is currently selected.
     * @param hasImage True if an image is selected.
     */
    private void updateUITexts(boolean hasImage) {
        if (hasImage) {
            textTitle.setText("Update Poster");
            textDescription.setText("Replace or remove the current poster.");
            uploadButton.setText("Update");
        } else {
            textTitle.setText("Upload Image");
            textDescription.setText("Upload the poster.");
            uploadButton.setText("Upload");
        }
    }

    /**
     * Clears the current image selection and resets the UI state locally.
     * Notifies the listener that the poster has been removed.
     */
    private void removePosterLocally() {
        currentUri = null;
        currentBase64 = null;
        imagePreview.setImageResource(R.drawable.img_poster);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btnDelete.setVisibility(View.GONE);
        updateUITexts(false);
        if (listener != null) listener.onPosterRemoved();
    }

    /**
     * Updates the preview ImageView with the selected URI.
     * @param uri The URI of the image to display.
     */
    private void updatePreview(Uri uri) {
        currentUri = uri;
        imagePreview.setImageURI(uri);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imagePreview.setImageTintList(null);
        btnDelete.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Set transparent background and adjust width
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
