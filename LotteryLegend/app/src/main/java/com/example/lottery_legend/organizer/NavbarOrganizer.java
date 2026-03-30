package com.example.lottery_legend.organizer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lottery_legend.entrant.ProfileActivity;
import com.example.lottery_legend.R;

/**
 * Utility class to manage the Organizer Navbar across different activities.
 */
public class NavbarOrganizer {

    public enum Tab {
        HOME, HISTORY, PROFILE
    }

    /**
     * Set up the navigation bar for an Organizer activity.
     *
     * @param activity   The current activity.
     * @param deviceId   The device ID of the user.
     * @param activeTab  The tab that should be highlighted.
     */
    public static void setup(Activity activity, String deviceId, Tab activeTab) {
        View navbar = activity.findViewById(R.id.navbar);
        if (navbar == null) return;

        View navHome = navbar.findViewById(R.id.navHome);
        View navHistory = navbar.findViewById(R.id.navHistory);
        View navProfile = navbar.findViewById(R.id.navProfile);

        ImageView imgHome = navbar.findViewById(R.id.imageNavHome);
        TextView txtHome = navbar.findViewById(R.id.textNavHome);
        ImageView imgHistory = navbar.findViewById(R.id.imageNavHistory);
        TextView txtHistory = navbar.findViewById(R.id.textNavHistory);
        ImageView imgProfile = navbar.findViewById(R.id.imageNavProfile);
        TextView txtProfile = navbar.findViewById(R.id.textNavProfile);

        // Reset colors
        int gray = Color.parseColor("#A7AAB3");
        int blue = Color.parseColor("#2563EB");

        imgHome.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHome.setTextColor(gray);
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
                Intent intent = new Intent(activity, OrganizerMainActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        navHistory.setOnClickListener(v -> {
            if (activeTab != Tab.HISTORY) {
                Intent intent = new Intent(activity, OrganizerHistoryActivity.class);
                intent.putExtra("deviceId", deviceId);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        navProfile.setOnClickListener(v -> {
            if (activeTab != Tab.PROFILE) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("isOrganizerMode", true);
                activity.startActivity(intent);
                activity.finish();
            }
        });
    }
}
