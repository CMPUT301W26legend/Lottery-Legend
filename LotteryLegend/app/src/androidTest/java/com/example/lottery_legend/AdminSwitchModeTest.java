package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;

import com.example.lottery_legend.admin.AdminActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

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
 * UI Test for US 03.09.01: Verifying that an administrator can switch to Entrant
 * and Organizer modes using their admin profile.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminSwitchModeTest {

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
        adminData.put("email", "admin@test.com");
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(adminData), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testSwitchToEntrantMode() throws InterruptedException {
        // Navigate to Profile tab
        onView(withId(R.id.nav_admin_profile)).perform(click());
        Thread.sleep(1000);

        // Verify we are on the profile fragment
        onView(withId(R.id.admin_viewName)).check(matches(withText("Test Admin")));

        // Click Switch to Entrant Mode
        onView(withId(R.id.layoutSwitchToEntrant)).perform(click());
        Thread.sleep(2000);

        // Verify we land on Entrant home screen (MainActivity)
        onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
    }

    @Test
    public void testSwitchToOrganizerMode() throws InterruptedException {
        // Navigate to Profile tab
        onView(withId(R.id.nav_admin_profile)).perform(click());
        Thread.sleep(1000);

        // Click Switch to Organizer Mode
        onView(withId(R.id.layoutSwitchToOrganizer)).perform(click());
        Thread.sleep(2000);

        // Verify we land on Organizer home screen (OrganizerMainActivity)
        onView(withId(R.id.ButtonCreateEvent)).check(matches(isDisplayed()));
    }
}
