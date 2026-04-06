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
 * Utility class to manage the navigation bar for the Organizer interface.
 * Handles tab highlighting and navigation logic between different organizer screens.
 */
public class NavbarOrganizer {

    /**
     * Enum representing the different tabs available in the organizer navigation bar.
     */
    public enum Tab {
        /** The home tab, usually showing the list of active events. */
        HOME, 
        /** The history tab, showing past events. */
        HISTORY, 
        /** The profile tab for viewing and editing organizer information. */
        PROFILE
    }

    /**
     * Sets up the navigation bar for an Organizer activity.
     * Configures click listeners for each tab and highlights the currently active tab.
     *
     * @param activity   The current activity where the navbar is being initialized.
     * @param deviceId   The unique device identifier for the current user.
     * @param activeTab  The tab that should be visually highlighted as active.
     */
    public static void setup(Activity activity, String deviceId, Tab activeTab) {
        View navbar = activity.findViewById(R.id.navbar);
        if (navbar == null) return;

        // Find navigation containers
        View navHome = navbar.findViewById(R.id.navHome);
        View navHistory = navbar.findViewById(R.id.navHistory);
        View navProfile = navbar.findViewById(R.id.navProfile);

        // Find navigation icons and text labels
        ImageView imgHome = navbar.findViewById(R.id.imageNavHome);
        TextView txtHome = navbar.findViewById(R.id.textNavHome);
        ImageView imgHistory = navbar.findViewById(R.id.imageNavHistory);
        TextView txtHistory = navbar.findViewById(R.id.textNavHistory);
        ImageView imgProfile = navbar.findViewById(R.id.imageNavProfile);
        TextView txtProfile = navbar.findViewById(R.id.textNavProfile);

        // Define colors for active and inactive states
        int gray = Color.parseColor("#A7AAB3");
        int blue = Color.parseColor("#2563EB");

        // Reset all tabs to the inactive (gray) state
        imgHome.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHome.setTextColor(gray);
        imgHistory.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtHistory.setTextColor(gray);
        imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(gray));
        txtProfile.setTextColor(gray);

        // Highlight the specified active tab in blue
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

        // Set up click listeners to navigate between activities
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
