package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.CreateEventActivity;
import com.example.lottery_legend.organizer.OrganizerEventDetailsActivity;
import com.example.lottery_legend.organizer.OrganizerHistoryActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Enhanced UI tests for OrganizerHistoryActivity, following the pattern of AdminBrowseProfilesTest.
 * Verifies Firestore data rendering and navigation to event details/creation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerHistoryActivityTest {

    private static final String TAG = "OrganizerHistoryTest";
    private static final String TEST_DEVICE_ID = "test-organizer-id-history";
    private static final String TEST_EVENT_ID = "test-event-history-unique-id";
    private static final String TEST_EVENT_TITLE = "History Integration Test Event";
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<OrganizerHistoryActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerHistoryActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        Intents.init();
        db = FirebaseFirestore.getInstance();

        // Create a test event in Firestore owned by the test organizer
        Event testEvent = new Event();
        testEvent.setOrganizerId(TEST_DEVICE_ID);
        testEvent.setTitle(TEST_EVENT_TITLE);
        testEvent.setDescription("Integration Test Description");
        testEvent.setGeoEnabled(false);
        testEvent.setEventLocation(new Event.EventLocation("Test Location", "Test Location", 0.0, 0.0));
        testEvent.setCapacity(10);
        testEvent.setMaxWaitingList(20);
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setStatus("open");

        // Await synchronization with Firestore to ensure data is present when activity loads
        // Use a generous timeout for network-dependent setup
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 20, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
        // Clean up the test data from Firestore with a robust timeout and error handling
        if (db != null) {
            try {
                // Cleanup should not crash the test if it fails due to network/timeout
                Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 20, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "Firestore cleanup failed or timed out: " + e.getMessage());
            }
        }
    }

    @Test
    public void testUIComponentsVisible() {
        onView(withId(R.id.recyclerOrganizerEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
        // Use R.id.navbar because the include tag in the activity layout overrides the root ID of the navbar layout
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventIsLoadedAndDisplayed() {
        // Scroll to the specific test event in the RecyclerView and verify its title is displayed
        onView(withId(R.id.recyclerOrganizerEvents))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(TEST_EVENT_TITLE))));
        
        onView(withText(TEST_EVENT_TITLE)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToCreateEventActivity() {
        // Click the create event button and verify the correct intent is fired
        onView(withId(R.id.createEventButton)).perform(click());
        
        intended(allOf(
                hasComponent(CreateEventActivity.class.getName()),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }

    @Test
    public void testNavigationToEventDetailsActivity() {
        // Click on the test event in the list to trigger navigation to its details
        onView(withId(R.id.recyclerOrganizerEvents))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(TEST_EVENT_TITLE)),
                        click()
                ));

        // Verify that the details activity is launched with the correct parameters
        intended(allOf(
                hasComponent(OrganizerEventDetailsActivity.class.getName()),
                hasExtra("eventId", TEST_EVENT_ID),
                hasExtra("eventTitle", TEST_EVENT_TITLE),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }
}
