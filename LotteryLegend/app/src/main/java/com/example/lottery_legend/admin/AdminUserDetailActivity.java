package com.example.lottery_legend.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottery_legend.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

/**
 * This class is the activity for the user detail view in the AdminUsersFragment.
 * It handles displaying the details of a user and the ability to delete them.
 */
public class AdminUserDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId, name, email, phone, collectionName, profileImage;
    private ShapeableImageView userProfileImage;

    /**
     * This runs when the Activity is created. It pulls the user's info from the Intent
     * and displays it on the screen. Also sets up the back button and the delete button.
     * @param savedInstanceState Data from the previous state of this screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
        collectionName = getIntent().getStringExtra("collectionName");
        profileImage = getIntent().getStringExtra("profileImage");

        TextView topTitle = findViewById(R.id.admin_top_title);
        TextView topSubtitle = findViewById(R.id.admin_top_subtitle);
        ImageButton btnBack = findViewById(R.id.btn_back_detail);
        TextView tvName = findViewById(R.id.user_name_detail);
        TextView tvEmail = findViewById(R.id.user_email_detail);
        TextView tvPhone = findViewById(R.id.user_phone_detail);
        userProfileImage = findViewById(R.id.profile_image_detail);
        Button btnDelete = findViewById(R.id.btn_delete_user);

        if (topTitle != null) topTitle.setText("Lottery Legend");
        if (topSubtitle != null) topSubtitle.setText("Administrator");
        
        tvName.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(phone);

        if (profileImage != null && !profileImage.isEmpty()) {
            displayBase64Image(userProfileImage, profileImage);
        } else {
            userProfileImage.setImageResource(R.drawable.ic_profile_avatar);
        }

        if ("organizers".equals(collectionName)) {
            btnDelete.setText("Delete Organizer");
        }
        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    /**
     * Helper method to decode Base64 string into a Bitmap and set it to the ImageView.
     * @param imageView The ImageView to display the image.
     * @param base64 The Base64 encoded image string.
     */
    private void displayBase64Image(ShapeableImageView imageView, String base64) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_profile_avatar);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_profile_avatar);
        }
    }

    /**
     * This shows a custom popup window to confirm if the Admin really
     * wants to delete the user. It changes the message based on whether
     * the user is an Entrant or an Organizer. If confirmed it deletes
     * the user from Firestore.
     */
    private void showDeleteDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_delete, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);

        if ("entrants".equals(collectionName)) {
            title.setText("Delete Entrant");
            message.setText("This will permanently remove this entrant.");
        } else {
            title.setText("Delete Organizer");
            message.setText("This will permanently remove this organizer.");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            if (userId != null) {
                deleteUserWithSubcollections(dialog);
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void deleteUserWithSubcollections(AlertDialog dialog) {
        WriteBatch batch = db.batch();
        String subCollection = "entrants".equals(collectionName) ? "notifications" : "createdEvents";

        db.collection(collectionName).document(userId).collection(subCollection).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(db.collection(collectionName).document(userId));
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminUserDetailActivity.this, "User removed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AdminUserDetailActivity.this, "Error removing user", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                })
                .addOnFailureListener(e -> {
                    // Even if subcollection fetch fails, try deleting the user document
                    db.collection(collectionName).document(userId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminUserDetailActivity.this, "User removed", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                finish();
                            });
                });
    }
}
