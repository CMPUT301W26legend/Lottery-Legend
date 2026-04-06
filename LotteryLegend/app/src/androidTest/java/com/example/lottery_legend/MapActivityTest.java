package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.event.MapActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * UI Test for MapActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapActivityTest {

    @Test
    public void testViewModeSingleMarker() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_TITLE, "Test Single Marker");
        intent.putExtra(MapActivity.EXTRA_LATITUDE, 53.5461);
        intent.putExtra(MapActivity.EXTRA_LONGITUDE, -113.4938);
        intent.putExtra(MapActivity.EXTRA_MARKER_NAME, "Edmonton");
        intent.putExtra(MapActivity.EXTRA_PICK_MODE, false);

        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
            // Check Toolbar title
            onView(withId(R.id.toolbarMap)).check(matches(isDisplayed()));
            onView(withText("Test Single Marker")).check(matches(isDisplayed()));

            // Map fragment should be displayed
            onView(withId(R.id.map)).check(matches(isDisplayed()));

            // Confirm button should be hidden in view mode
            onView(withId(R.id.btnConfirmPick)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void testPickMode() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_TITLE, "Pick a Point");
        intent.putExtra(MapActivity.EXTRA_PICK_MODE, true);

        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
            // Check Toolbar title
            onView(withText("Pick a Point")).check(matches(isDisplayed()));

            // Map fragment should be displayed
            onView(withId(R.id.map)).check(matches(isDisplayed()));

            // Confirm button should be visible in pick mode
            onView(withId(R.id.btnConfirmPick)).check(matches(isDisplayed()));
            onView(withId(R.id.btnConfirmPick)).check(matches(withText("Confirm Location")));
        }
    }

    @Test
    public void testViewModeMultipleMarkers() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_TITLE, "Multiple Users");
        
        ArrayList<Double> lats = new ArrayList<>();
        lats.add(53.5);
        lats.add(53.6);
        
        ArrayList<Double> lngs = new ArrayList<>();
        lngs.add(-113.4);
        lngs.add(-113.5);
        
        ArrayList<String> names = new ArrayList<>();
        names.add("User 1");
        names.add("User 2");

        intent.putExtra(MapActivity.EXTRA_LATITUDES, lats);
        intent.putExtra(MapActivity.EXTRA_LONGITUDES, lngs);
        intent.putExtra(MapActivity.EXTRA_NAMES, names);
        intent.putExtra(MapActivity.EXTRA_PICK_MODE, false);

        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withText("Multiple Users")).check(matches(isDisplayed()));
            onView(withId(R.id.map)).check(matches(isDisplayed()));
            onView(withId(R.id.btnConfirmPick)).check(matches(not(isDisplayed())));
        }
    }
}
