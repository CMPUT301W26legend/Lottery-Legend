package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lottery_legend.R;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The main dashboard for users in the Organizer role.
 */
public class OrganizerMainActivity extends AppCompatActivity {

    Button ButtonCreateEvent;
    private FirebaseFirestore db;
    private String deviceId;


    /**
     * Called when the activity is first created.
     * Initializes the UI, configures the organizer navigation bar, and sets up
     * the event creation navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ButtonCreateEvent = findViewById(R.id.ButtonCreateEvent);
        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HOME);

        ButtonCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainActivity.this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }
}
