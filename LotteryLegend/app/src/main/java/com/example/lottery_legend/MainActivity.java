package com.example.lottery_legend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("MainActivity", "Listen failed.", error);
                return;
            }

            allEvents.clear();

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Event event = doc.toObject(Event.class);
                    if (!event.getOrganizerId().equals(deviceId)) {
                        allEvents.add(event);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });
        setupNavbar();
    }

    private void setupNavbar() {
        View navbar = findViewById(R.id.navbar);
        if (navbar != null) {
            ImageView imageHome = navbar.findViewById(R.id.imageNavHome);
            TextView textHome = navbar.findViewById(R.id.textNavHome);
            imageHome.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));
            textHome.setTextColor(Color.parseColor("#2563EB"));

            View profileNav = navbar.findViewById(R.id.navProfile);
            profileNav.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            });

            View scanNav = navbar.findViewById(R.id.navScan);
            if (scanNav != null) {
                scanNav.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                });
            }
        }
    }
}