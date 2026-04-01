package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.EditEventActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test for EditEventActivity.
 * Tests the ability to load and update event details.
 */
@RunWith(AndroidJUnit4.class)
public class EditEventActivityTest {

    private String testEventId = "test-edit-event-id";
    private String deviceId;
    private FirebaseFirestore db;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_MEDIA_IMAGES);

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(ApplicationProvider.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Create a dummy event to edit
        Event dummyEvent = new Event();
        dummyEvent.setEventId(testEventId);
        dummyEvent.setOrganizerId(deviceId);
        dummyEvent.setTitle("Original Title");
        dummyEvent.setDescription("Original Description");
        dummyEvent.setPrice(10.0);
        dummyEvent.setCapacity(50);
        dummyEvent.setMaxWaitingList(100);
        
        // Correct constructor: EventLocation(String name, String address, Double latitude, Double longitude)
        dummyEvent.setEventLocation(new Event.EventLocation("Original Location", "123 Test St", 0.0, 0.0));
        
        Timestamp now = Timestamp.now();
        dummyEvent.setRegistrationStartAt(now);
        dummyEvent.setRegistrationEndAt(new Timestamp(new Date(now.getSeconds() * 1000 + 3600000))); // +1h
        dummyEvent.setDrawAt(new Timestamp(new Date(now.getSeconds() * 1000 + 7200000))); // +2h
        dummyEvent.setEventStartAt(new Timestamp(new Date(now.getSeconds() * 1000 + 10800000))); // +3h
        dummyEvent.setEventEndAt(new Timestamp(new Date(now.getSeconds() * 1000 + 14400000))); // +4h
        
        dummyEvent.setWaitingList(new ArrayList<>());
        dummyEvent.setStatus("open");

        // Wait for setup to complete in Firestore
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").document(testEventId).set(dummyEvent).addOnCompleteListener(task -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        Intents.release();
        // Clean up test data
        db.collection("events").document(testEventId).delete();
    }

    @Test
    public void testLoadEventData() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditEventActivity.class);
        intent.putExtra("eventId", testEventId);
        intent.putExtra("deviceId", deviceId);

        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for data to load from Firestore
            Thread.sleep(3000);

            // Check if fields are pre-filled correctly
            onView(withId(R.id.editTextEventTitle)).perform(scrollTo()).check(matches(withText("Original Title")));
            onView(withId(R.id.editTextDescription)).perform(scrollTo()).check(matches(withText("Original Description")));
            onView(withId(R.id.editTextLocation)).perform(scrollTo()).check(matches(withText("Original Location")));
            onView(withId(R.id.editTextPrice)).perform(scrollTo()).check(matches(withText("10.0")));
            onView(withId(R.id.Capacity)).perform(scrollTo()).check(matches(withText("50")));
            onView(withId(R.id.WaitingList)).perform(scrollTo()).check(matches(withText("100")));
        }
    }

    @Test
    public void testUpdateEventDetails() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditEventActivity.class);
        intent.putExtra("eventId", testEventId);
        intent.putExtra("deviceId", deviceId);

        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for data to load
            Thread.sleep(3000);

            // Edit the title using replaceText for better reliability
            onView(withId(R.id.editTextEventTitle)).perform(scrollTo(), replaceText("Updated Title"), closeSoftKeyboard());
            
            // Edit the description
            onView(withId(R.id.editTextDescription)).perform(scrollTo(), replaceText("Updated Description"), closeSoftKeyboard());

            // Click save
            onView(withId(R.id.createButton)).perform(scrollTo(), click());

            // Small delay for Firestore update to propagate
            Thread.sleep(3000);

            // Verify update in Firestore
            CountDownLatch latch = new CountDownLatch(1);
            final String[] updatedData = new String[2];
            db.collection("events").document(testEventId).get().addOnSuccessListener(doc -> {
                Event updated = doc.toObject(Event.class);
                if (updated != null) {
                    updatedData[0] = updated.getTitle();
                    updatedData[1] = updated.getDescription();
                }
                latch.countDown();
            });
            
            latch.await(5, TimeUnit.SECONDS);
            assertEquals("Updated Title", updatedData[0]);
            assertEquals("Updated Description", updatedData[1]);
        }
    }

    @Test
    public void testUpdatePosterButtonText() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditEventActivity.class);
        intent.putExtra("eventId", testEventId);
        intent.putExtra("deviceId", deviceId);

        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for data to load
            Thread.sleep(2000);

            // This is a simplified test for the button presence
            onView(withId(R.id.uploadButton)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }
}
