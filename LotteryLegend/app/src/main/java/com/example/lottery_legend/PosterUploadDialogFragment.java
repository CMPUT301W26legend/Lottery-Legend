package com.example.lottery_legend;

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

public class PosterUploadDialogFragment extends DialogFragment {

    public interface OnPosterEventListener {
        void onPosterSelected(Uri uri);
        void onPosterRemoved();
    }

    private OnPosterEventListener listener;
    private Uri currentUri;
    private ImageView imagePreview;
    private ImageView btnDelete;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    updatePreview(uri);
                }
            }
    );

    public void setOnPosterEventListener(OnPosterEventListener listener) {
        this.listener = listener;
    }

    public void setCurrentUri(Uri uri) {
        this.currentUri = uri;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_poster_dialog, container, false);

        imagePreview = view.findViewById(R.id.imagePosterPreview);
        btnDelete = view.findViewById(R.id.btnDeletePoster);
        Button uploadButton = view.findViewById(R.id.btnUploadConfirm);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (currentUri != null) {
            updatePreview(currentUri);
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

    private void removePosterLocally() {
        currentUri = null;
        imagePreview.setImageResource(R.drawable.img_poster);
        imagePreview.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btnDelete.setVisibility(View.GONE);
        if (listener != null) listener.onPosterRemoved();
    }

    private void updatePreview(Uri uri) {
        currentUri = uri;
        imagePreview.setImageURI(uri);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btnDelete.setVisibility(View.VISIBLE);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
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
