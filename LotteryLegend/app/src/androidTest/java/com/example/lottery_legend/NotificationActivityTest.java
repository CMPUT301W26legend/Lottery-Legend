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

import com.example.lottery_legend.entrant.NotificationActivity;
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
 * UI Test for NotificationActivity.
 * Manual launch ensures Firestore data is ready.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotificationActivityTest {

    private static final String TEST_DEVICE_ID = "test-device-id-notifications";
    private static final String TEST_NOTIFICATION_ID = "test-notif-1";
    private FirebaseFirestore db;

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // 1. Create a test notification in Firestore
        Notification notif = new Notification();
        notif.setNotificationId(TEST_NOTIFICATION_ID);
        notif.setRecipientId(TEST_DEVICE_ID);
        notif.setTitle("Test Notification Title");
        notif.setMessage("This is a test notification message for UI testing.");
        notif.setIsRead(false);
        notif.setCreatedAt(Timestamp.now());
        notif.setType("GENERAL");

        Tasks.await(db.collection("notifications").document(TEST_NOTIFICATION_ID).set(notif), 10, TimeUnit.SECONDS);
        
        // 2. Ensure entrant exists and has notifications enabled
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(new java.util.HashMap<String, Object>() {{
            put("notificationsEnabled", true);
            put("deviceId", TEST_DEVICE_ID);
        }}), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("notifications").document(TEST_NOTIFICATION_ID).delete(), 10, TimeUnit.SECONDS);
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testUIComponentsVisible() {
        try (ActivityScenario<NotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbarNotification)).check(matches(isDisplayed()));
            onView(withId(R.id.layoutUnreadSummary)).check(matches(isDisplayed()));
            onView(withId(R.id.filterScroll)).check(matches(isDisplayed()));
            onView(withId(R.id.rvNotifications)).check(matches(isDisplayed()));
            onView(withId(R.id.navbar)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testNotificationDisplay() throws InterruptedException {
        try (ActivityScenario<NotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for Firestore listener to trigger
            Thread.sleep(2000);
            
            onView(withText("Test Notification Title")).check(matches(isDisplayed()));
            onView(withText("This is a test notification message for UI testing.")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testFilterTabs() {
        try (ActivityScenario<NotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.tabAll)).check(matches(isDisplayed()));
            onView(withId(R.id.tabUnread)).check(matches(isDisplayed()));
            onView(withId(R.id.tabRead)).check(matches(isDisplayed()));

            onView(withId(R.id.tabUnread)).perform(click());
            // Filter logic is internal, but we verify the tab is clickable
            
            onView(withId(R.id.tabRead)).perform(click());
        }
    }

    @Test
    public void testMarkAllReadButton() {
        try (ActivityScenario<NotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.tvMarkAll)).check(matches(isDisplayed()));
            onView(withId(R.id.tvMarkAll)).perform(click());
        }
    }

    @Test
    public void testNotificationClickOpensDialog() throws InterruptedException {
        try (ActivityScenario<NotificationActivity> scenario = ActivityScenario.launch(createIntent())) {
            Thread.sleep(2000);
            
            onView(withText("Test Notification Title")).perform(click());
            
            // Verify status dialog appears (General notification type)
            onView(withId(R.id.tvCancelTitle)).check(matches(isDisplayed()));
            onView(withText("Test Notification Title")).check(matches(isDisplayed()));
            onView(withId(R.id.btnCancelDismiss)).perform(click());
        }
    }
}
