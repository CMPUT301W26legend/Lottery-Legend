package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
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
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.ProfileActivity;
import com.example.lottery_legend.event.EventDetailsActivity;
import com.example.lottery_legend.event.ShareQRCodeActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced UI tests for EventDetailsActivity (Entrant perspective).
 * Verifies data rendering, navigation, and waiting list interaction.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventDetailsActivityTest {

    private static final String TAG = "EventDetailsTest";
    private static final String TEST_EVENT_ID = "test-event-details-id";
    private static final String TEST_DEVICE_ID = "test-entrant-device-id";
    private static final String TEST_ORGANIZER_ID = "test-organizer-id";
    private static final String TEST_TITLE = "Entrant Test Event";
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<EventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        Intents.init();
        db = FirebaseFirestore.getInstance();

        // Create a test event in Firestore
        Timestamp now = new Timestamp(new Date());
        Event testEvent = new Event();
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setOrganizerId(TEST_ORGANIZER_ID);
        testEvent.setTitle(TEST_TITLE);
        testEvent.setDescription("Integration Description for Event Details Test.");
        testEvent.setStatus("open");
        testEvent.setEventLocation(new Event.EventLocation("Test Venue", "123 Test St", 0.0, 0.0));
        testEvent.setCapacity(50);
        testEvent.setPrice(10.0);
        testEvent.setEventStartAt(now);
        testEvent.setRegistrationEndAt(now);
        testEvent.setDrawAt(now);
        testEvent.setWaitingList(new ArrayList<>());

        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
        if (db != null) {
            try {
                Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 10, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "Cleanup failed: " + e.getMessage());
            }
        }
    }

    @Test
    public void testUIComponentsVisible() throws InterruptedException {
        // Wait for Firestore data to load and UI to settle
        Thread.sleep(2000);
        
        onView(withId(R.id.toolbarEventDetails)).check(matches(isDisplayed()));
        onView(withId(R.id.textEventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.btnJoinWaitingList)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventDataDisplayed() throws InterruptedException {
        // Wait for Firestore snapshot listener to populate UI
        Thread.sleep(2000);
        
        onView(withId(R.id.textEventTitle)).check(matches(withText(TEST_TITLE)));
        onView(withId(R.id.textLocation)).check(matches(withText("Test Venue")));
        onView(withId(R.id.textCapacity)).check(matches(withText("50 Spots")));
        onView(withId(R.id.textRegistrationStatus)).check(matches(withText("Open")));
        onView(withId(R.id.btnJoinWaitingList)).perform(scrollTo()).check(matches(withText("Join Waiting List")));
    }

    @Test
    public void testNavigationToShareQRCode() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.shareIcon)).perform(click());
        
        intended(allOf(
                hasComponent(ShareQRCodeActivity.class.getName()),
                hasExtra("eventId", TEST_EVENT_ID),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }

    @Test
    public void testNavigationToOrganizerProfile() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.layoutOrganizerProfile)).perform(scrollTo(), click());

        intended(allOf(
                hasComponent(ProfileActivity.class.getName()),
                hasExtra("deviceId", TEST_ORGANIZER_ID),
                hasExtra("isReadOnly", true),
                hasExtra("isOrganizerMode", true)
        ));
    }

    @Test
    public void testJoinWaitingListDialog() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.btnJoinWaitingList)).perform(scrollTo(), click());
        
        // Verify dialog is shown
        onView(withText("Join Waiting List")).check(matches(isDisplayed()));
    }

    @Test
    public void testAlreadyJoinedState() throws Exception {
        // Update Firestore to show user is already joined
        Timestamp now = new Timestamp(new Date());
        Event testEvent = new Event();
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setOrganizerId(TEST_ORGANIZER_ID);
        testEvent.setTitle(TEST_TITLE);
        testEvent.setStatus("open");
        
        List<Event.WaitingListEntry> waitingList = new ArrayList<>();
        waitingList.add(new Event.WaitingListEntry(TEST_DEVICE_ID, now, now, "waiting", null, null, null, null, null, null, null, null, false, 0, false, null, null, null));
        testEvent.setWaitingList(waitingList);
        
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 10, TimeUnit.SECONDS);

        // Wait for UI update
        Thread.sleep(2000);

        onView(withId(R.id.textRegistrationStatus)).check(matches(withText("Joined")));
        onView(withId(R.id.btnJoinWaitingList)).perform(scrollTo()).check(matches(withText("Leave Waiting List")));
    }
}
