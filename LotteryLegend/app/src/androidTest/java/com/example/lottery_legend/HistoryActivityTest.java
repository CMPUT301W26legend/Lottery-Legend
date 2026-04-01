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

import com.example.lottery_legend.entrant.HistoryActivity;
import com.example.lottery_legend.event.EventDetailsActivity;
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
 * UI tests for HistoryActivity, verifying tab filtering and event display.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HistoryActivityTest {

    private static final String TAG = "HistoryActivityTest";
    private static final String TEST_DEVICE_ID = "test-entrant-history-id";
    private static final String EVENT_WAITING_ID = "event-waiting-id";
    private static final String EVENT_ACCEPTED_ID = "event-accepted-id";
    
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<HistoryActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        Intents.init();
        db = FirebaseFirestore.getInstance();

        // Setup mock events in Firestore
        setupMockEvents();
    }

    private void setupMockEvents() throws Exception {
        Timestamp now = new Timestamp(new Date());

        // Event where entrant is in Waiting status
        Event waitingEvent = new Event();
        waitingEvent.setEventId(EVENT_WAITING_ID);
        waitingEvent.setTitle("Waiting Event");
        waitingEvent.setStatus("open");
        List<Event.WaitingListEntry> waitingList1 = new ArrayList<>();
        waitingList1.add(new Event.WaitingListEntry(TEST_DEVICE_ID, now, now, "waiting", null, null, null, null, null, null, null, null, false, 0, false, null, null, null));
        waitingEvent.setWaitingList(waitingList1);

        // Event where entrant is in Accepted status
        Event acceptedEvent = new Event();
        acceptedEvent.setEventId(EVENT_ACCEPTED_ID);
        acceptedEvent.setTitle("Accepted Event");
        acceptedEvent.setStatus("open");
        List<Event.WaitingListEntry> waitingList2 = new ArrayList<>();
        waitingList2.add(new Event.WaitingListEntry(TEST_DEVICE_ID, now, now, "accepted", null, null, null, null, null, null, null, null, false, 0, false, null, null, null));
        acceptedEvent.setWaitingList(waitingList2);

        Tasks.await(db.collection("events").document(EVENT_WAITING_ID).set(waitingEvent), 10, TimeUnit.SECONDS);
        Tasks.await(db.collection("events").document(EVENT_ACCEPTED_ID).set(acceptedEvent), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
        if (db != null) {
            try {
                Tasks.await(db.collection("events").document(EVENT_WAITING_ID).delete(), 10, TimeUnit.SECONDS);
                Tasks.await(db.collection("events").document(EVENT_ACCEPTED_ID).delete(), 10, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "Cleanup failed: " + e.getMessage());
            }
        }
    }

    @Test
    public void testUIComponentsVisible() {
        onView(withId(R.id.toolbarHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayoutHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testWaitingTabFiltering() throws InterruptedException {
        // Give time for Firestore snapshot listener to fire
        Thread.sleep(2000);

        // Should see the waiting event
        onView(withId(R.id.recyclerHistory))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("Waiting Event"))));
        onView(withText("Waiting Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testTabSwitching() throws InterruptedException {
        Thread.sleep(2000);

        // Switch to Accepted tab
        onView(withText("Accepted")).perform(click());
        Thread.sleep(1000);

        // Should see the accepted event
        onView(withId(R.id.recyclerHistory))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("Accepted Event"))));
        onView(withText("Accepted Event")).check(matches(isDisplayed()));
        
        // Switch back to Waiting tab
        onView(withText("Waiting")).perform(click());
        Thread.sleep(1000);
        onView(withText("Waiting Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToEventDetails() throws InterruptedException {
        Thread.sleep(2000);

        // Click on the waiting event
        onView(withId(R.id.recyclerHistory))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Waiting Event")),
                        click()
                ));

        // Verify navigation to EventDetailsActivity
        intended(allOf(
                hasComponent(EventDetailsActivity.class.getName()),
                hasExtra("eventId", EVENT_WAITING_ID),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }
}
