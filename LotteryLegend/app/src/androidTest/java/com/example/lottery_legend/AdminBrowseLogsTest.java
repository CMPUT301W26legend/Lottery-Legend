package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Organizer;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * UI Test for US 03.08.01: Browsing notification logs as an administrator.
 * Modified to use Firebase Local Emulator with authorized test user.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminBrowseLogsTest {

    private FirebaseFirestore db;
    private static final String TEST_DEVICE_ID = "test_admin_123";
    private static final String TEST_EVENT_ID = "log_test_event_id";
    private static final String TEST_SENDER_ID = "log_test_sender_id";
    private static final String TEST_NOTIFICATION_ID = "log_test_notification_id";

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
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), AdminActivity.class)
                    .putExtra("deviceId", TEST_DEVICE_ID));

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("isAdmin", true);
        adminData.put("name", "Test Admin");
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(adminData), 5, TimeUnit.SECONDS);

        Event testEvent = new Event();
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setTitle("Notification Test Event");
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 5, TimeUnit.SECONDS);

        Organizer testOrganizer = new Organizer();
        testOrganizer.setDeviceId(TEST_SENDER_ID);
        testOrganizer.setName("Log Sender Name");
        testOrganizer.setCreatedEvents(new ArrayList<>());
        Tasks.await(db.collection("organizers").document(TEST_SENDER_ID).set(testOrganizer), 5, TimeUnit.SECONDS);

        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", TEST_EVENT_ID);
        notification.put("senderId", TEST_SENDER_ID);
        notification.put("recipientId", "some_recipient");
        notification.put("title", "Test Log Title");
        notification.put("message", "This is a detailed test log message.");
        notification.put("type", "LOTTERY_WIN");
        notification.put("createdAt", Timestamp.now());
        Tasks.await(db.collection("notifications").document(TEST_NOTIFICATION_ID).set(notification), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            db.collection("notifications").document(TEST_NOTIFICATION_ID).delete();
            db.collection("events").document(TEST_EVENT_ID).delete();
            db.collection("organizers").document(TEST_SENDER_ID).delete();
            db.collection("entrants").document(TEST_DEVICE_ID).delete();
        }
    }

    @Test
    public void testBrowseLogsAndSeeDetails() throws InterruptedException {
        onView(withId(R.id.nav_admin_logs)).perform(click());

        Thread.sleep(4000);

        onView(withText("Notification Test Event")).check(matches(isDisplayed()));
        onView(withText("Log Sender Name")).check(matches(isDisplayed()));
        onView(withText("Selected Users")).check(matches(isDisplayed()));
        onView(withText("Notification Test Event")).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.detail_event_title)).check(matches(withText("Notification Test Event")));
        onView(withId(R.id.detail_sender_name)).check(matches(withText("Log Sender Name")));
        onView(withId(R.id.detail_msg_title)).check(matches(withText("Test Log Title")));
        onView(withId(R.id.detail_msg_body)).check(matches(withText("This is a detailed test log message.")));

        onView(withId(R.id.btn_close_detail)).perform(click());
    }
}
