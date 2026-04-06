package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.event.EventNotificationActivity;
import com.example.lottery_legend.model.Notification;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * UI Test for EventNotificationActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventNotificationActivityTest {

    private static final String TEST_DEVICE_ID = "test-device-id-event-notif";
    private static final String TEST_EVENT_ID = "test-event-id-123";
    private static final String TEST_ORGANIZER_ID = "test-org-id-456";
    private static final String TEST_NOTIFICATION_ID = "test-event-notif-1";
    private FirebaseFirestore db;

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventNotificationActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("organizerId", TEST_ORGANIZER_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Setup a specific event notification in Firestore
        Notification notif = new Notification();
        notif.setNotificationId(TEST_NOTIFICATION_ID);
        notif.setRecipientId(TEST_DEVICE_ID);
        notif.setRecipientType("ENTRANT");
        notif.setEventId(TEST_EVENT_ID);
        notif.setSenderId(TEST_ORGANIZER_ID);
        notif.setTitle("Event Specific Notification");
        notif.setMessage("Important update about your event.");
        notif.setIsRead(false);
        notif.setCreatedAt(Timestamp.now());
        notif.setType("GENERAL");

        Tasks.await(db.collection("notifications").document(TEST_NOTIFICATION_ID).set(notif), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("notifications").document(TEST_NOTIFICATION_ID).delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testUIComponentsVisible() {
        try (ActivityScenario<EventNotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbarNotification)).check(matches(isDisplayed()));
            onView(withText("Event Notifications")).check(matches(isDisplayed()));
            onView(withId(R.id.layoutUnreadSummary)).check(matches(isDisplayed()));
            onView(withId(R.id.rvNotifications)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testEventNotificationDisplay() throws InterruptedException {
        try (ActivityScenario<EventNotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for Firestore to fetch
            Thread.sleep(2000);
            
            onView(withText("Event Specific Notification")).check(matches(isDisplayed()));
            onView(withText("Important update about your event.")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testUnreadSummaryText() throws InterruptedException {
        try (ActivityScenario<EventNotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            Thread.sleep(2000);
            onView(withId(R.id.tvUnreadSummary)).check(matches(withText("You have 1 unread event notifications")));
        }
    }

    @Test
    public void testMarkAllAsRead() {
        try (ActivityScenario<EventNotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.tvMarkAll)).perform(click());
            // Verification of state change happens via toast or data update (hard to check directly in Espresso)
        }
    }

    @Test
    public void testFilterTabs() {
        try (ActivityScenario<EventNotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.tabUnread)).perform(click());
            onView(withId(R.id.tabRead)).perform(click());
            onView(withId(R.id.tabAll)).perform(click());
        }
    }
}
