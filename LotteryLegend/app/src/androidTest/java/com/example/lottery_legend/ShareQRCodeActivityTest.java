package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.event.ShareQRCodeActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * UI tests for ShareQRCodeActivity.
 * Verifies that the event title and QR code image are displayed correctly,
 * and that navigation works as expected.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShareQRCodeActivityTest {

    private static final String TAG = "ShareQRCodeTest";
    private static final String TEST_EVENT_ID = "test-event-share-id";
    private static final String TEST_DEVICE_ID = "test-entrant-device-id";
    private static final String TEST_TITLE = "QR Share Test Event";
    // Small valid base64 encoded PNG for testing
    private static final String TEST_QR_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<ShareQRCodeActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ShareQRCodeActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Create a test event with a QR code image in Firestore
        Event testEvent = new Event();
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setTitle(TEST_TITLE);
        testEvent.setQrCodeImage(TEST_QR_BASE64);
        testEvent.setStatus("open");

        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            try {
                Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 10, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "Cleanup failed: " + e.getMessage());
            }
        }
    }

    @Test
    public void testUIComponentsVisible() {
        onView(withId(R.id.toolbarShare)).check(matches(isDisplayed()));
        onView(withId(R.id.textEventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.imageQrCode)).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventDataDisplayed() throws InterruptedException {
        // Wait for Firestore data to load
        Thread.sleep(2000);
        
        onView(withId(R.id.textEventTitle)).check(matches(withText(TEST_TITLE)));
    }

    @Test
    public void testBackButtonFinishesActivity() {
        // Click the navigation (back) button in the toolbar
        // The navigation button is an ImageButton that is a child of the Toolbar
        onView(allOf(isAssignableFrom(ImageButton.class), withParent(withId(R.id.toolbarShare))))
                .perform(click());
        
        // Use the scenario to verify the activity is finishing/finished
        activityRule.getScenario().onActivity(activity -> {
            assertTrue(activity.isFinishing() || activity.isDestroyed());
        });
    }
}
