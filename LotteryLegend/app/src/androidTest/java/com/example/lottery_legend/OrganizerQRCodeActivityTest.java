package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.OrganizerQRCodeActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * UI Test for OrganizerQRCodeActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerQRCodeActivityTest {

    private static final String TEST_EVENT_ID = "test-qr-event-id";
    private static final String TEST_EVENT_TITLE = "QR Test Event";
    private FirebaseFirestore db;
    private String dummyQrBase64;

    @Rule
    public ActivityScenarioRule<OrganizerQRCodeActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerQRCodeActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("eventTitle", TEST_EVENT_TITLE);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Create a dummy QR code string (Base64)
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        dummyQrBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        // Setup initial test data in Firestore
        Event testEvent = new Event(
                "testOrganizerId",
                TEST_EVENT_TITLE,
                "Description for QR Test",
                false,
                "Test Location",
                "2025-01-01",
                "2025-01-02",
                "2024-12-01",
                "2024-12-31",
                "2025-01-01",
                100,
                200
        );
        testEvent.setEventId(TEST_EVENT_ID);
        
        // Wait for Firestore operations to complete
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 10, TimeUnit.SECONDS);
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).update("qrCodeImage", dummyQrBase64), 10, TimeUnit.SECONDS);
        
        // Brief sleep to allow Firestore to propagate
        Thread.sleep(1000);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testUIComponentsVisible() {
        // Verify all key UI components are present
        onView(withId(R.id.toolbarOrganizerQr)).check(matches(isDisplayed()));
        onView(withId(R.id.cardQrCode)).check(matches(isDisplayed()));
        onView(withId(R.id.imageQrCode)).check(matches(isDisplayed()));
        onView(withId(R.id.textEventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventTitleDisplay() {
        // Verify that the title passed in the intent is displayed correctly
        onView(withId(R.id.textEventTitle)).check(matches(withText(TEST_EVENT_TITLE)));
    }

    @Test
    public void testQRCodeImageLoads() throws InterruptedException {
        // Since we set up dummyQrBase64 in Before, the activity should load it.
        // We wait a moment for the Firestore callback and image decoding.
        Thread.sleep(2000);
        onView(withId(R.id.imageQrCode)).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarContent() {
        // Verify toolbar title and subtitle if applicable
        onView(withText("QR Code")).check(matches(isDisplayed()));
        onView(withText("Share your event")).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarNavigationClick() {
        // Verify navigation icon is clickable (simulating back press)
        onView(withId(R.id.toolbarOrganizerQr)).perform(click());
        // Since it calls finish(), the activity will be destroyed.
    }

    @Test
    public void testNavbarIsDisplayed() {
        // Verify the organizer navbar is visible at the bottom
        onView(withId(R.id.navHome)).check(matches(isDisplayed()));
        onView(withId(R.id.navHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.navProfile)).check(matches(isDisplayed()));
    }
}
