package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        deviceId = getIntent().getStringExtra("deviceId");

        editTextName = findViewById(R.id.etName);
        editTextEmail = findViewById(R.id.etEmail);
        editTextPhone = findViewById(R.id.etPhone);
        saveButton = findViewById(R.id.btnSave);

        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        if (entrant != null) {
                            editTextName.setText(entrant.name);
                            editTextEmail.setText(entrant.email);
                            editTextPhone.setText(entrant.phone);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());

        saveButton.setOnClickListener(v -> saveProfileData());

        setupNavbar();
    }

    /**
     * Update the profile information in Firestore.
     */
    private void saveProfileData() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in Name and Email", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("entrants").document(deviceId)
                .update("name", name,
                        "email", email,
                        "phone", phone)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupNavbar() {
        View navbar = findViewById(R.id.navbar);
        if (navbar != null) {
            ImageView imageProfile = navbar.findViewById(R.id.imageNavProfile);
            TextView textProfile = navbar.findViewById(R.id.textNavProfile);
            imageProfile.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2563EB")));
            textProfile.setTextColor(android.graphics.Color.parseColor("#2563EB"));

            View homeItem = navbar.findViewById(R.id.navHome);
            homeItem.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
                finish();
            });
        }
    }
}