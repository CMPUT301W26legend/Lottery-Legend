package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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
    }

    @Test
    public void testNavigationToCreateEvent() {
        // Click on Create Event button
        onView(withId(R.id.ButtonCreateEvent)).perform(click());

        // Verify that the correct Intent was sent to start CreateEventActivity
        intended(hasComponent(CreateEventActivity.class.getName()));
        intended(hasExtra("deviceId", TEST_DEVICE_ID));
    }
}
