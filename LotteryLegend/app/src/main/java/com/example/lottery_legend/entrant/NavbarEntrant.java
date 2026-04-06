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
 * Utility class to manage the Entrant Navigation Bar across different activities.
 * Handles highlighting of active tabs and navigation between main entrant screens.
 */
public class NavbarEntrant {

    /**
     * Enumeration of available tabs in the entrant navigation bar.
     */
    public enum Tab {
        HOME, SCAN, HISTORY, PROFILE
    }

    /**
     * Configures the navigation bar for the provided activity.
     * Sets active colors and attaches click listeners to navigation items.
     *
     * @param activity   The current activity context.
     * @param deviceId   The unique identifier for the user's device.
     * @param activeTab  The tab currently active in the UI.
     */
    public static void setup(Activity activity, String deviceId, Tab activeTab) {
        View navbar = activity.findViewById(R.id.navbar);
        if (navbar == null) return;

        // Obtain references to navigation item containers
        View navHome = navbar.findViewById(R.id.navHome);
        View navScan = navbar.findViewById(R.id.navScan);
        View navHistory = navbar.findViewById(R.id.navHistory);
        View navProfile = navbar.findViewById(R.id.navProfile);

        // Obtain references to icons and text labels
        ImageView imgHome = navbar.findViewById(R.id.imageNavHome);
        TextView txtHome = navbar.findViewById(R.id.textNavHome);
        ImageView imgScan = navbar.findViewById(R.id.imageNavScan);
        TextView txtScan = navbar.findViewById(R.id.textNavScan);
        ImageView imgHistory = navbar.findViewById(R.id.imageNavHistory);
        TextView txtHistory = navbar.findViewById(R.id.textNavHistory);
        ImageView imgProfile = navbar.findViewById(R.id.imageNavProfile);
        TextView txtProfile = navbar.findViewById(R.id.textNavProfile);

        // Define primary navigation colors
        int gray = Color.parseColor("#A7AAB3");
        int blue = Color.parseColor("#2563EB");

        // Reset all navigation items to the inactive (gray) state
        imgHome.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHome.setTextColor(gray);
        imgScan.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtScan.setTextColor(gray);
        imgHistory.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHistory.setTextColor(gray);
        imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtProfile.setTextColor(gray);

        // Apply the active (blue) color to the selected tab
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

        // Set Click Listeners for each navigation item
        navHome.setOnClickListener(v -> {
            if (activeTab != Tab.HOME) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                // Finish calling activity if it's not the main dashboard
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
