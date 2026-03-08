package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is the activity for creating a profile.
 * It will save the profile to the database.
 */

public class CreateProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private com.google.android.material.switchmaterial.SwitchMaterial switchNotification;
    private Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect to Firestore database
        db = FirebaseFirestore.getInstance();

        // Get device ID
        deviceId = getIntent().getStringExtra("deviceID");

        nameEditText = findViewById(R.id.etName);
        emailEditText = findViewById(R.id.etEmail);
        phoneEditText = findViewById(R.id.etPhone);
        switchNotification = findViewById(R.id.switchNotification);
        saveButton = findViewById(R.id.btnSave);

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            boolean notification = switchNotification.isChecked();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // Create a new user
            Entrant user = new Entrant(name, email, phone, notification);

            // Add a new document with a generated ID
            db.collection("entrants").document(deviceId).set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}