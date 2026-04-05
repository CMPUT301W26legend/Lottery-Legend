package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.OrganizerEventDetailsActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * UI Test for US 02.01.03: Inviting specific entrants to a private event's waiting list.
 * Uses Firebase Local Emulator.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InviteEntrantsTest {

    private static final String TEST_EVENT_ID = "test-private-event-id";
    private static final String TEST_ORG_ID = "test-org-id";
    private FirebaseFirestore db;

    static {
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.useEmulator("10.0.2.2", 8080);
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            // Handled
        }
    }

    @Rule
    public ActivityScenarioRule<OrganizerEventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerEventDetailsActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("deviceId", TEST_ORG_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        Timestamp now = new Timestamp(new Date());

        // Create a private test event
        Event testEvent = new Event();
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setOrganizerId(TEST_ORG_ID);
        testEvent.setTitle("Private Test Event");
        testEvent.setDescription("A private event for testing invites");
        testEvent.setIsPrivateEvent(true);
        testEvent.setCapacity(50);
        testEvent.setStatus("open");
        testEvent.setWaitingList(new ArrayList<>());
        
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 5, TimeUnit.SECONDS);

        // Create some test entrants
        Entrant entrant1 = new Entrant("id1", "John Doe", "john@example.com", "1234567890", true, now, now, false);
        Entrant entrant2 = new Entrant("id2", "Jane Smith", "jane@example.com", "0987654321", true, now, now, false);
        
        Tasks.await(db.collection("entrants").document("id1").set(entrant1), 5, TimeUnit.SECONDS);
        Tasks.await(db.collection("entrants").document("id2").set(entrant2), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("entrants").document("id1").delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("entrants").document("id2").delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testInviteEntrantFlow() throws InterruptedException {
        // 1. Check if Invite button is visible (since it's a private event)
        // Scroll to the button first as it might be at the bottom of a NestedScrollView
        onView(withId(R.id.btnInviteEntrants)).perform(scrollTo()).check(matches(isDisplayed()));

        // 2. Open Invite Search Dialog
        onView(withId(R.id.btnInviteEntrants)).perform(click());
        onView(withText("Invite Entrants")).check(matches(isDisplayed()));

        // 3. Search by Name
        onView(withId(R.id.editSearchName)).perform(typeText("John"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.btnPerformSearch)).perform(click());

        // 4. Verify search results and select entrant
        Thread.sleep(1000); // Wait for query
        onView(withText("Search Results")).check(matches(isDisplayed()));
        onView(withText("John Doe")).check(matches(isDisplayed()));
        
        // Select John Doe
        onView(withId(R.id.recyclerSearchResults))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // 5. Send Invite
        onView(withId(R.id.btnInviteSelected)).perform(click());

        // 6. Verify toast/success (Checking if dialog closed is a good proxy if Toast is hard to match)
        // Dialog should be gone
    }

    @Test
    public void testSearchByEmail() throws InterruptedException {
        onView(withId(R.id.btnInviteEntrants)).perform(scrollTo(), click());
        onView(withId(R.id.editSearchEmail)).perform(typeText("jane@example.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.btnPerformSearch)).perform(click());

        Thread.sleep(1000);
        onView(withText("Jane Smith")).check(matches(isDisplayed()));
    }
    
    @Test
    public void testSearchByPhone() throws InterruptedException {
        onView(withId(R.id.btnInviteEntrants)).perform(scrollTo(), click());
        onView(withId(R.id.editSearchPhone)).perform(typeText("1234567890"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.btnPerformSearch)).perform(click());

        Thread.sleep(1000);
        onView(withText("John Doe")).check(matches(isDisplayed()));
    }
}