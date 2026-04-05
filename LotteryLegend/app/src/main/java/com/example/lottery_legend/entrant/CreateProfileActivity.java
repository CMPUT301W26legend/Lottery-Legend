package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Entrant;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
    private ImageView imgAvatar;
    private TextView tvUploadPhoto;
    private String profileImageBase64;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processAndSetImage(uri);
                }
            }
    );


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
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);

        tvUploadPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        imgAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            boolean notification = switchNotification.isChecked();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Timestamp now = Timestamp.now();
            Entrant user = new Entrant(deviceId, name, email, phone, notification, now, now, false, profileImageBase64);

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

    private void processAndSetImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap != null) {
                // Resize if too large
                int maxWidth = 500;
                int maxHeight = 500;
                if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                    float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(ratio * bitmap.getWidth()), Math.round(ratio * bitmap.getHeight()), true);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                profileImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                
                imgAvatar.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
}
