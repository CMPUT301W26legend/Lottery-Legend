package com.example.lottery_legend.admin;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class is the activity for the user detail view in the AdminUsersFragment.
 * It handles displaying the details of a user and the ability to delete them.
 */
public class AdminUserDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId, name, email, phone, collectionName;

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

        TextView topTitle = findViewById(R.id.admin_top_title);
        TextView topSubtitle = findViewById(R.id.admin_top_subtitle);
        ImageButton btnBack = findViewById(R.id.btn_back_detail);
        TextView tvName = findViewById(R.id.user_name_detail);
        TextView tvEmail = findViewById(R.id.user_email_detail);
        TextView tvPhone = findViewById(R.id.user_phone_detail);
        Button btnDelete = findViewById(R.id.btn_delete_user);

        if (topTitle != null) topTitle.setText("Lottery Legend");
        if (topSubtitle != null) topSubtitle.setText("Administrator");
        
        tvName.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(phone);

        if ("organizers".equals(collectionName)) {
            btnDelete.setText("Delete Organizer");
        }
        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
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
                db.collection(collectionName).document(userId).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AdminUserDetailActivity.this, "User removed", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdminUserDetailActivity.this, "Error removing user", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }
}