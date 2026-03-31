package com.example.lottery_legend.entrant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.ScanActivity;

/**
 * Utility class to manage the Entrant Navbar across different activities.
 */
public class NavbarEntrant {

    public enum Tab {
        HOME, SCAN, HISTORY, PROFILE
    }

    /**
     * Set up the navigation bar for an Entrant activity.
     *
     * @param activity   The current activity.
     * @param deviceId   The device ID of the user.
     * @param activeTab  The tab that should be highlighted.
     */
    public static void setup(Activity activity, String deviceId, Tab activeTab) {
        View navbar = activity.findViewById(R.id.navbar);
        if (navbar == null) return;

        View navHome = navbar.findViewById(R.id.navHome);
        View navScan = navbar.findViewById(R.id.navScan);
        View navHistory = navbar.findViewById(R.id.navHistory);
        View navProfile = navbar.findViewById(R.id.navProfile);

        ImageView imgHome = navbar.findViewById(R.id.imageNavHome);
        TextView txtHome = navbar.findViewById(R.id.textNavHome);
        ImageView imgScan = navbar.findViewById(R.id.imageNavScan);
        TextView txtScan = navbar.findViewById(R.id.textNavScan);
        ImageView imgHistory = navbar.findViewById(R.id.imageNavHistory);
        TextView txtHistory = navbar.findViewById(R.id.textNavHistory);
        ImageView imgProfile = navbar.findViewById(R.id.imageNavProfile);
        TextView txtProfile = navbar.findViewById(R.id.textNavProfile);

        // Colors
        int gray = Color.parseColor("#A7AAB3");
        int blue = Color.parseColor("#2563EB");

        // Reset all to gray
        imgHome.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHome.setTextColor(gray);
        imgScan.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtScan.setTextColor(gray);
        imgHistory.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHistory.setTextColor(gray);
        imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtProfile.setTextColor(gray);

        // Highlight active tab
        switch (activeTab) {
            case HOME:
                imgHome.setImageTintList(android.content.res.ColorStateList.valueOf(blue));
                txtHome.setTextColor(blue);
                break;
            case SCAN:
                imgScan.setImageTintList(android.content.res.ColorStateList.valueOf(blue));
                txtScan.setTextColor(blue);
                break;
            case HISTORY:
                imgHistory.setImageTintList(android.content.res.ColorStateList.valueOf(blue));
                txtHistory.setTextColor(blue);
                break;
            case PROFILE:
                imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(blue));
                txtProfile.setTextColor(blue);
                break;
        }

        // Set Click Listeners
        navHome.setOnClickListener(v -> {
            if (activeTab != Tab.HOME) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                if (!(activity instanceof MainActivity)) activity.finish();
            }
        });

        navScan.setOnClickListener(v -> {
            if (activeTab != Tab.SCAN) {
                Intent intent = new Intent(activity, ScanActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                if (!(activity instanceof MainActivity)) activity.finish();
            }
        });

        navHistory.setOnClickListener(v -> {
            if (activeTab != Tab.HISTORY) {
                Intent intent = new Intent(activity, HistoryActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                if (!(activity instanceof HistoryActivity)) activity.finish();
            }
        });

        navProfile.setOnClickListener(v -> {
            if (activeTab != Tab.PROFILE) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                if (!(activity instanceof MainActivity)) activity.finish();
            }
        });
    }
}
