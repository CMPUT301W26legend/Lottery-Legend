package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    
    private TextView viewName;
    private TextView viewEmail;
    private TextView viewPhone;
    private SwitchMaterial switchNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        deviceId = getIntent().getStringExtra("deviceId");

        viewName = findViewById(R.id.viewName);
        viewEmail = findViewById(R.id.viewEmail);
        viewPhone = findViewById(R.id.viewPhone);
        switchNotifications = findViewById(R.id.switchNotifications);

        fetchProfileData();
        setupNavbar();
    }

    private void fetchProfileData() {
        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        if (entrant != null) {
                            viewName.setText(entrant.name);
                            viewEmail.setText(entrant.email);

                            if (entrant.phone != null && !entrant.phone.isEmpty()) {
                                viewPhone.setText(entrant.phone);
                            } else {
                                viewPhone.setText("No phone number provided");
                            }

                            switchNotifications.setChecked(entrant.notification);
                        }
                    } else {
                        Log.d("ProfileActivity", "No such document");
                        Toast.makeText(ProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching document", e);
                    Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
                finish();
            });
        }
    }
}