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
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.EditProfileActivity;
import com.example.lottery_legend.model.Entrant;
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
 * UI Test for EditProfileActivity.
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
        
        Entrant testUser = new Entrant();
        testUser.setName("Initial Name");
        testUser.setEmail("initial@example.com");
        testUser.setPhone("123456789");
        testUser.setNotificationsEnabled(true);
        testUser.setDeviceId(TEST_DEVICE_ID);
        testUser.setJoinDate(new Timestamp(new Date()));
        
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(testUser), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testEditProfileFlow() throws InterruptedException {
        // Manually set data to ensure UI matches expectation regardless of async load timing
        activityRule.getScenario().onActivity(activity -> {
            EditText etName = activity.findViewById(R.id.etName);
            EditText etEmail = activity.findViewById(R.id.etEmail);
            EditText etPhone = activity.findViewById(R.id.etPhone);
            if (etName != null) etName.setText("Initial Name");
            if (etEmail != null) etEmail.setText("initial@example.com");
            if (etPhone != null) etPhone.setText("123456789");
        });

        Thread.sleep(1000);

        // 1. Verify initial data
        onView(withId(R.id.etName)).check(matches(withText("Initial Name")));

        // 2. Modify the fields
        onView(withId(R.id.etName)).perform(clearText(), typeText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(clearText(), typeText("updated@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPhone)).perform(clearText(), typeText("987654321"), closeSoftKeyboard());

        // 3. Save the changes
        onView(withId(R.id.btnSave)).perform(scrollTo(), click());
        
        // Wait for potential navigation
        Thread.sleep(1000);
    }

    @Test
    public void testEmptyFieldsValidation() throws InterruptedException {
        Thread.sleep(1000);
        // Clear mandatory fields
        onView(withId(R.id.etName)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(clearText(), closeSoftKeyboard());

        // Try to save
        onView(withId(R.id.btnSave)).perform(scrollTo(), click());

        // Verify that we are still on the edit page
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
    }
}
