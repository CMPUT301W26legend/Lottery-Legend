package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.ProfileActivity;
import com.example.lottery_legend.organizer.CreateEventActivity;
import com.example.lottery_legend.organizer.OrganizerHistoryActivity;
import com.example.lottery_legend.organizer.OrganizerMainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Test for OrganizerMainActivity, focused on navigation and intent verification.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerMainActivityTest {

    private static final String TEST_DEVICE_ID = "test-organizer-id";

    @Rule
    public ActivityScenarioRule<OrganizerMainActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerMainActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testOrganizerMainActivityLaunch() {
        // Verify main UI components
        onView(withId(R.id.ButtonCreateEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
        
        // Verify navbar components
        onView(withId(R.id.navHome)).check(matches(isDisplayed()));
        onView(withId(R.id.navHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.navProfile)).check(matches(isDisplayed()));
        
        // Verify text in navbar
        onView(withText("Home")).check(matches(isDisplayed()));
        onView(withText("History")).check(matches(isDisplayed()));
        onView(withText("Profile")).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToCreateEvent() {
        // Click on Create Event button
        onView(withId(R.id.ButtonCreateEvent)).perform(click());

        // Verify that the correct Intent was sent to start CreateEventActivity
        intended(allOf(
                hasComponent(CreateEventActivity.class.getName()),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }

    @Test
    public void testNavigationToHistory() {
        // Click on History tab in navbar
        onView(withId(R.id.navHistory)).perform(click());
        
        // Verify navigation to OrganizerHistoryActivity
        intended(allOf(
                hasComponent(OrganizerHistoryActivity.class.getName()),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }

    @Test
    public void testNavigationToProfile() {
        // Click on Profile tab in navbar
        onView(withId(R.id.navProfile)).perform(click());
        
        // Verify navigation to ProfileActivity
        intended(allOf(
                hasComponent(ProfileActivity.class.getName()),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }
}
