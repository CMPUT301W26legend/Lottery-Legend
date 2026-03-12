package com.example.lottery_legend;

import android.os.Bundle;
import android.view.View;
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

        View topBar = findViewById(R.id.admin_top_bar);
        if (topBar != null) {
            TextView topTitle = topBar.findViewById(R.id.admin_top_title);
            TextView topSubtitle = topBar.findViewById(R.id.admin_top_subtitle);

            BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_admin_events) {
                    selectedFragment = new AdminEventsFragment();
                    if (topTitle != null) topTitle.setText("Lottery Legend");
                    if (topSubtitle != null) topSubtitle.setText("Administrator");
                } else if (itemId == R.id.nav_admin_users) {
                    selectedFragment = new AdminUsersFragment();
                    if (topTitle != null) topTitle.setText("Lottery Legend");
                    if (topSubtitle != null) topSubtitle.setText("Administrator");
                } else if (itemId == R.id.nav_admin_media) {
                    selectedFragment = new AdminMediaFragment();
                    if (topTitle != null) topTitle.setText("Images");
                    if (topSubtitle != null) topSubtitle.setText("Administrator");
                } else if (itemId == R.id.nav_admin_logs) {
                    selectedFragment = new AdminLogsFragment();
                    if (topTitle != null) topTitle.setText("Notification Logs");
                    if (topSubtitle != null) topSubtitle.setText("Administrator");
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
}