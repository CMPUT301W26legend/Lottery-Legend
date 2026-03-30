package com.example.lottery_legend;

import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;

import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test for US 03.04.01: Browsing events as an administrator.
 * Modified to use Firebase Local Emulator with authorized test user.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminBrowseEventsTest {

    private FirebaseFirestore db;
    private static final String TEST_DEVICE_ID = "test_admin_123";

    static {
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.useEmulator("10.0.2.2", 8080);
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            // Handle or ignore
        }
    }
    
    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), AdminActivity.class)
                    .putExtra("deviceId", TEST_DEVICE_ID));

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Register the test device as an admin in the local emulator
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("isAdmin", true);
        adminData.put("name", "Test Admin");
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(adminData), 5, TimeUnit.SECONDS);

        Event testEvent = new Event(
                "testEventBrowsingID",     // eventId
                "testOrganizerId",         // organizerId
                "testEventBrowsing",       // title
                "This is a test description", // description
                null,                      // eventLocation
                0.0,                       // price
                false,                     // isPrivateEvent
                false,                     // geoEnabled
                null,                      // eventStartAt
                null,                      // eventEndAt
                null,                      // registrationStartAt
                null,                      // registrationEndAt
                null,                      // drawAt
                100,                       // capacity
                200,                       // maxWaitingList
                0,                         // waitingListCount
                0,                         // selectedCount
                0,                         // cancelledCount
                0,                         // enrolledCount
                null,                      // posterImage
                null,                      // qrCodeImage
                null,                      // qrCodeValue
                null,                      // lotteryGuidelines
                "open",                    // status
                null,                      // createdAt
                null,                      // updatedAt
                null,                      // waitingList
                null,                      // comments
                null,                      // coOrganizers
                null                       // tickets
        );

        Tasks.await(db.collection("events").document("testEventBrowsingID").set(testEvent), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("events").document("testEventBrowsingID").delete(), 10, TimeUnit.SECONDS);
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testBrowseEvents() throws InterruptedException {
        onView(withId(R.id.nav_admin_events)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.admin_events_recycler)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_events_recycler))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("testEventBrowsing"))));

        onView(withText("testEventBrowsing")).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToEventDetail() throws InterruptedException {
        onView(withId(R.id.nav_admin_events)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.admin_events_recycler))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("testEventBrowsing")), click()));

        Thread.sleep(2000);

        onView(withId(R.id.event_title_detail)).check(matches(withText("testEventBrowsing")));
        onView(withId(R.id.detail_about_event)).check(matches(isDisplayed()));
    }
}
