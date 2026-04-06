package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.WaitingListActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * UI Test for WaitingListActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WaitingListActivityTest {

    private static final String TEST_EVENT_ID = "test-event-waiting-list-ui";
    private static final String TEST_ENTRANT_ID_1 = "test-entrant-1";
    private static final String TEST_ENTRANT_ID_2 = "test-entrant-2";
    private static final String TEST_DEVICE_ID = "test-organizer-id";
    private FirebaseFirestore db;

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, WaitingListActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Setup test entrants
        Entrant entrant1 = new Entrant();
        entrant1.setDeviceId(TEST_ENTRANT_ID_1);
        entrant1.setName("Waiting Entrant");
        entrant1.setNotificationsEnabled(true);

        Entrant entrant2 = new Entrant();
        entrant2.setDeviceId(TEST_ENTRANT_ID_2);
        entrant2.setName("Selected Entrant");
        entrant2.setNotificationsEnabled(true);

        Tasks.await(db.collection("entrants").document(TEST_ENTRANT_ID_1).set(entrant1), 10, TimeUnit.SECONDS);
        Tasks.await(db.collection("entrants").document(TEST_ENTRANT_ID_2).set(entrant2), 10, TimeUnit.SECONDS);

        // Setup a test event with a waiting list
        Event event = new Event();
        event.setEventId(TEST_EVENT_ID);
        event.setTitle("Test Waiting List Event");
        event.setOrganizerId(TEST_DEVICE_ID);
        
        List<Event.WaitingListEntry> waitingList = new ArrayList<>();
        
        Event.WaitingListEntry entry1 = new Event.WaitingListEntry();
        entry1.setDeviceId(TEST_ENTRANT_ID_1);
        entry1.setParticipationStatus("waiting");
        entry1.setJoinedAt(Timestamp.now());
        waitingList.add(entry1);

        Event.WaitingListEntry entry2 = new Event.WaitingListEntry();
        entry2.setDeviceId(TEST_ENTRANT_ID_2);
        entry2.setParticipationStatus("invited");
        entry2.setJoinedAt(Timestamp.now());
        waitingList.add(entry2);
        
        event.setWaitingList(waitingList);

        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(event), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            db.collection("events").document(TEST_EVENT_ID).delete();
            db.collection("entrants").document(TEST_ENTRANT_ID_1).delete();
            db.collection("entrants").document(TEST_ENTRANT_ID_2).delete();
        }
    }

    @Test
    public void testUIComponentsVisible() {
        try (ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbarWaitingList)).check(matches(isDisplayed()));
            onView(withId(R.id.textEntrantCount)).check(matches(isDisplayed()));
            onView(withId(R.id.editSearchEntrants)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonFilter)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerWaitingList)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testEntrantsDisplay() {
        try (ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for data to load
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            
            onView(withText("Waiting Entrant")).check(matches(isDisplayed()));
            onView(withText("Waiting")).check(matches(isDisplayed()));
            onView(withText("Promote to Co-organizer")).check(matches(isDisplayed()));

            onView(withText("Selected Entrant")).check(matches(isDisplayed()));
            onView(withText("Selected")).check(matches(isDisplayed()));
            onView(withText("Cancel Selection")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSearchFunctionality() {
        try (ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for data to load
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            onView(withId(R.id.editSearchEntrants)).perform(typeText("Waiting"), closeSoftKeyboard());
            onView(withText("Waiting Entrant")).check(matches(isDisplayed()));
            onView(withId(R.id.textEntrantCount)).check(matches(withText(containsString("1 entrants"))));

            onView(withId(R.id.editSearchEntrants)).perform(typeText("NonExistent"), closeSoftKeyboard());
            onView(withId(R.id.textEntrantCount)).check(matches(withText(containsString("0 entrants"))));
        }
    }

    @Test
    public void testFilterMenu() {
        try (ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.buttonFilter)).perform(click());
            onView(withText("Waiting")).check(matches(isDisplayed()));
            onView(withText("Selected")).check(matches(isDisplayed()));
            onView(withText("Accepted")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPromoteDialog() {
        try (ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for data to load
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            onView(withText("Promote to Co-organizer")).perform(click());
            // Verify promote dialog content
            onView(withId(R.id.textPromoteTitle)).check(matches(withText("Promote to Co-organizer")));
            onView(withId(R.id.buttonConfirmPromote)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonCancelPromote)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testCancelSelectionDialog() {
        try (ActivityScenario<WaitingListActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for data to load
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            onView(withText("Cancel Selection")).perform(click());
            // Verify cancel selection dialog content
            onView(withId(R.id.textCancelTitle)).check(matches(withText("Cancel Selection")));
            onView(withId(R.id.buttonConfirm)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonCancel)).check(matches(isDisplayed()));
        }
    }
}
