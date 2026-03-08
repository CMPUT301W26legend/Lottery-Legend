package com.example.lottery_legend;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        deviceId = getIntent().getStringExtra("deviceId");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavbar();
    }

    private void setupNavbar() {
        View navbar = findViewById(R.id.navbar);
        if (navbar != null) {
            ImageView imageHome = navbar.findViewById(R.id.imageNavHome);
            TextView textHome = navbar.findViewById(R.id.textNavHome);

            imageHome.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2563EB")));
            textHome.setTextColor(Color.parseColor("#2563EB"));
            
            View profileNav = navbar.findViewById(R.id.navProfile);
            profileNav.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            });
        }
    }
}