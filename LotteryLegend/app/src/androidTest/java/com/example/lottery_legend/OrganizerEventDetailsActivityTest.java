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

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.OrganizerEventDetailsActivity;
import com.example.lottery_legend.organizer.OrganizerQRCodeActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Enhanced UI tests for OrganizerEventDetailsActivity, including Firebase data interaction,
 * following the pattern of AdminBrowseProfilesTest.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerEventDetailsActivityTest {

    private static final String TEST_EVENT_ID = "test-event-id-details";
    private static final String TEST_EVENT_TITLE = "Integration Test Event";
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<OrganizerEventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerEventDetailsActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("eventTitle", TEST_EVENT_TITLE);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        Intents.init();
        db = FirebaseFirestore.getInstance();
        
        // Create a test event in Firestore to simulate real data environment
        Event testEvent = new Event("org1", TEST_EVENT_TITLE, "Integration Description", true, "Test Location",
                "2023-10-01", "2023-10-02", "2023-09-01", "2023-09-30", "2023-10-01", 10, 20);
        testEvent.setEventId(TEST_EVENT_ID);
        
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
        if (db != null) {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testUIComponentsVisible() {
        onView(withId(R.id.toolbarOrganizerDetails)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbarTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.shareIcon)).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testTitleDisplayedCorrectly() {
        onView(withId(R.id.toolbarTitle)).check(matches(withText("Details: " + TEST_EVENT_TITLE)));
    }

    @Test
    public void testNavigationToQRCodeActivity() {
        onView(withId(R.id.shareIcon)).perform(click());
        intended(allOf(
                hasComponent(OrganizerQRCodeActivity.class.getName()),
                hasExtra("eventId", TEST_EVENT_ID),
                hasExtra("eventTitle", TEST_EVENT_TITLE)
        ));
    }
}
