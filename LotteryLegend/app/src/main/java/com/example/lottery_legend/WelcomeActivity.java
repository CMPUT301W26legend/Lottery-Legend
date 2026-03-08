package com.example.lottery_legend;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is the welcome screen of the app.
 * This screen will check if the user has already created a profile on this phone.
 */
public class WelcomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Button createProfileButton;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect to Firestore database
        db = FirebaseFirestore.getInstance();

        // Get device ID
        // https://www.geeksforgeeks.org/android/how-to-get-the-unique-id-of-an-android-device/
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check if the entrant already has a profile
        checkExistingUser();

        createProfileButton = findViewById(R.id.CreateProfileButton);
        createProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateProfileActivity.class);
            intent.putExtra("deviceID", deviceId);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Check if the device ID is already in the database. If the device ID is found, goes to MainActivity.
     */
    public void checkExistingUser() {
        db.collection("entrants").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User is already in the database, go to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
                finish();
            } else {
                createProfileButton.setEnabled(true);
            }
        }).addOnFailureListener(e -> {
            createProfileButton.setEnabled(true);
        });
    }
}
