package com.example.lottery_legend;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The main activity of the application for entrants.
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private RecyclerView eventView;
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        deviceId = getIntent().getStringExtra("deviceId");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventView = findViewById(R.id.eventView);
        eventView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(allEvents, deviceId);
        eventView.setAdapter(adapter);

        // Set up a real-time listener on the "events" collection in Firestore
        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("MainActivity", "Listen failed.", error);
                return;
            }

            allEvents.clear();

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    // Only show events created by other users
                    if (event.getOrganizerId() != null && !event.getOrganizerId().equals(deviceId)) {
                        allEvents.add(event);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });

        // Use the common Navbar class for Entrants
        NavbarEntrant.setup(this, deviceId, NavbarEntrant.Tab.HOME);
    }
}
