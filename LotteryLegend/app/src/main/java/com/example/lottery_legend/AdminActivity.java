package com.example.lottery_legend;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        TextView topTitle = findViewById(R.id.admin_top_title);
        TextView topSubtitle = findViewById(R.id.admin_top_subtitle);

        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_admin_events) {
                selectedFragment = new Fragment();
                topTitle.setText("Lottery Legend");
                topSubtitle.setText("Administrator");
            } else if (itemId == R.id.nav_admin_users) {
                selectedFragment = new AdminUsersFragment();
                topTitle.setText("Lottery Legend");
                topSubtitle.setText("Administrator");
            } else if (itemId == R.id.nav_admin_media) {
                selectedFragment = new Fragment();
                topTitle.setText("Images");
                topSubtitle.setText("Administrator");
            } else if (itemId == R.id.nav_admin_logs) {
                selectedFragment = new Fragment();
                topTitle.setText("Notification Logs");
                topSubtitle.setText("Administrator");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_admin_events);
        }
    }
}