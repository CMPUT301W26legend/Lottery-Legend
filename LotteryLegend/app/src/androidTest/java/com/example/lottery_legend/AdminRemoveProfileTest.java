package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;

import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Organizer;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test for US 03.02.01: Removing user profiles as an administrator.
 * Modified to use Firebase Local Emulator with authorized test user.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminRemoveProfileTest {

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
        Timestamp now = new Timestamp(new Date());

        // Register the test device as an admin in the local emulator
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("isAdmin", true);
        adminData.put("name", "Test Admin");
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(adminData), 5, TimeUnit.SECONDS);
        
        Entrant testEntrant = new Entrant(
                "entrantTest",
                "entrantTest",
                "remove@test.com",
                "1234567890",
                true,
                now,
                now,
                false
        );
        Tasks.await(db.collection("entrants").document("entrantTest").set(testEntrant), 5, TimeUnit.SECONDS);

        Organizer testOrganizer = new Organizer(
                "organizerTest",
                "organizerTest",
                "remove_org@test.com",
                "0987654321",
                now,
                now,
                false,
                new ArrayList<>()
        );
        Tasks.await(db.collection("organizers").document("organizerTest").set(testOrganizer), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("entrants").document("entrantTest").delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("organizers").document("organizerTest").delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testRemoveEntrantProfile() throws InterruptedException {
        onView(withId(R.id.nav_admin_users)).perform(click());
        Thread.sleep(2000);
        
        onView(withText("Entrants")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.admin_users_recycler))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("entrantTest"))));

        onView(withText("entrantTest")).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.remove_user_button), hasSibling(withText("entrantTest"))))
                .perform(click());

        onView(withId(R.id.btn_delete)).perform(click());
        Thread.sleep(2000);
        onView(withText("entrantTest")).check(doesNotExist());
    }

    @Test
    public void testRemoveOrganizerProfile() throws InterruptedException {
        onView(withId(R.id.nav_admin_users)).perform(click());
        Thread.sleep(2000);
        
        onView(withText("Organizers")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.admin_users_recycler))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("organizerTest"))));

        onView(withText("organizerTest")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.remove_user_button), hasSibling(withText("organizerTest"))))
                .perform(click());

        onView(withId(R.id.btn_delete)).perform(click());
        Thread.sleep(2000);
        onView(withText("organizerTest")).check(doesNotExist());
    }
}
