package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * UI Test for EditProfileActivity, modeled after AdminBrowseEventsTest.
 * This test pre-creates a test user in Firestore, modifies it via UI, and verifies the flow.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditProfileActivityTest {

    private static final String TEST_DEVICE_ID = "test-edit-profile-id";
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<EditProfileActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EditProfileActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        
        // Setup initial test data in Firestore (similar to how AdminBrowseEventsTest sets up a test event)
        // Correcting Entrant constructor usage
        Entrant testUser = new Entrant(
                "Initial Name", 
                "initial@example.com", 
                "123456789", 
                true, 
                TEST_DEVICE_ID, 
                new Timestamp(new Date())
        );
        
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(testUser), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            // Clean up the test user
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testEditProfileFlow() throws InterruptedException {
        // Wait for Firestore data to be fetched and populated in UI
        Thread.sleep(1500);

        // 1. Verify that the initial data is correctly loaded into the EditTexts
        onView(withId(R.id.etName)).check(matches(withText("Initial Name")));
        onView(withId(R.id.etEmail)).check(matches(withText("initial@example.com")));

        // 2. Modify the fields
        onView(withId(R.id.etName)).perform(clearText(), typeText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(clearText(), typeText("updated@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPhone)).perform(clearText(), typeText("987654321"), closeSoftKeyboard());

        // 3. Save the changes
        onView(withId(R.id.btnSave)).perform(scrollTo(), click());
    }

    @Test
    public void testEmptyFieldsValidation() {
        // Clear mandatory fields to trigger validation
        onView(withId(R.id.etName)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(clearText(), closeSoftKeyboard());

        // Try to save
        onView(withId(R.id.btnSave)).perform(scrollTo(), click());

        // Verify that we are still on the edit page (validation prevented navigation)
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
    }
}
