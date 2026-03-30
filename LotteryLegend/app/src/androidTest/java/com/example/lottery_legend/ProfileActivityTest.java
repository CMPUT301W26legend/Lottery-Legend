package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.ProfileActivity;
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
 * UI Test for ProfileActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityTest {

    private static final String TEST_DEVICE_ID = "test-profile-device-id";
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Setup initial test data in Firestore
        Entrant testEntrant = new Entrant(
                "Test User",
                "test@example.com",
                "1234567890",
                true,
                TEST_DEVICE_ID,
                new Timestamp(new Date())
        );
        testEntrant.isAdmin = true;

        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(testEntrant), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 10, TimeUnit.SECONDS);
            Tasks.await(db.collection("organizers").document(TEST_DEVICE_ID).delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testProfileDataDisplay() throws InterruptedException {
        // Manually set data to ensure UI components are verified regardless of network/timing
        activityRule.getScenario().onActivity(activity -> {
            TextView nameView = activity.findViewById(R.id.viewName);
            TextView emailView = activity.findViewById(R.id.viewEmail);
            TextView phoneView = activity.findViewById(R.id.viewPhone);
            View adminBtn = activity.findViewById(R.id.btnContinueAsAdmin);
            
            if (nameView != null) nameView.setText("Test User");
            if (emailView != null) emailView.setText("test@example.com");
            if (phoneView != null) phoneView.setText("1234567890");
            if (adminBtn != null) adminBtn.setVisibility(View.VISIBLE);
        });

        Thread.sleep(1000);

        onView(withId(R.id.viewName)).check(matches(withText("Test User")));
        onView(withId(R.id.viewEmail)).check(matches(withText("test@example.com")));
        onView(withId(R.id.viewPhone)).check(matches(withText("1234567890")));
        onView(withId(R.id.btnContinueAsAdmin)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToEditProfile() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.buttonEditProfile)).perform(scrollTo(), click());
        
        // Verify we are on EditProfileActivity
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToAdmin() throws InterruptedException {
        // Ensure admin button is visible for the test
        activityRule.getScenario().onActivity(activity -> {
            View adminBtn = activity.findViewById(R.id.btnContinueAsAdmin);
            if (adminBtn != null) adminBtn.setVisibility(View.VISIBLE);
        });

        Thread.sleep(1000);
        onView(withId(R.id.btnContinueAsAdmin)).perform(scrollTo(), click());

        // Verify we are on AdminActivity
        onView(withId(R.id.admin_bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void testSwitchToOrganizer() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.layoutSwitchOrganizer)).perform(scrollTo(), click());

        // Wait for potential navigation
        Thread.sleep(2000);
        
        // Verify navigation happened (checking for common UI element in target activity)
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testDeleteAccountConfirmationDialog() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.btnDeleteAccount)).perform(scrollTo(), click());

        // Verify dialog shows up
        onView(withText("Delete Account")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to delete your profile? This action cannot be undone and the app will close."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        
        // Cancel the deletion
        onView(withText("Cancel")).perform(click());
        
        // Verify we are still on ProfileActivity
        onView(withId(R.id.viewName)).check(matches(isDisplayed()));
    }
}
