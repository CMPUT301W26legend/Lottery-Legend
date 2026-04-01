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
 * A DialogFragment for organizers to upload, update, or remove an event poster image.
 */
public class PosterUploadDialogFragment extends DialogFragment {

    public interface OnPosterEventListener {
        void onPosterSelected(Uri uri);
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

    public void setOnPosterEventListener(OnPosterEventListener listener) {
        this.listener = listener;
    }

    public void setCurrentUri(Uri uri) {
        this.currentUri = uri;
    }

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

    private void removePosterLocally() {
        currentUri = null;
        currentBase64 = null;
        imagePreview.setImageResource(R.drawable.img_poster);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btnDelete.setVisibility(View.GONE);
        updateUITexts(false);
        if (listener != null) listener.onPosterRemoved();
    }

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
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
