package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test for US 03.10.01: As an administrator, I want to remove event comments that violate app policy.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminDeleteCommentTest {

    private FirebaseFirestore db;
    private static final String TEST_DEVICE_ID = "test_admin_123";
    private static final String TEST_EVENT_ID = "test_event_comment_del";
    private static final String TEST_COMMENT_ID = "test_comment_to_del";

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

        // Register the test device as an admin
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("isAdmin", true);
        adminData.put("name", "Test Admin");
        Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).set(adminData), 5, TimeUnit.SECONDS);

        // Create a test event
        Event testEvent = new Event();
        testEvent.setEventId(TEST_EVENT_ID);
        testEvent.setTitle("Comment Test Event");
        testEvent.setDescription("Testing comment deletion");
        testEvent.setOrganizerId("test_org_id");
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(testEvent), 5, TimeUnit.SECONDS);

        // Create a test comment
        Comment testComment = new Comment();
        testComment.setCommentId(TEST_COMMENT_ID);
        testComment.setAuthorId("user_123");
        testComment.setAuthorNameSnapshot("Bad User");
        testComment.setContent("Violating Policy Content");
        testComment.setCreatedAt(Timestamp.now());
        testComment.setAuthorType("ENTRANT");
        testComment.setThreadLevel(0);

        Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                .collection("comments").document(TEST_COMMENT_ID).set(testComment), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID).collection("comments").document(TEST_COMMENT_ID).delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("entrants").document(TEST_DEVICE_ID).delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testAdminDeleteComment() throws InterruptedException {
        // Navigate to Events tab
        onView(withId(R.id.nav_admin_events)).perform(click());
        Thread.sleep(2000);

        // Scroll to and click on the test event
        onView(withId(R.id.admin_events_recycler))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Comment Test Event")), click()));
        Thread.sleep(2000);

        // Click "View Comments"
        onView(withId(R.id.btn_view_comments_admin)).perform(click());
        Thread.sleep(2000);

        // Verify comment is there
        onView(withText("Violating Policy Content")).check(matches(isDisplayed()));

        // Click Delete button on the comment
        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Violating Policy Content")),
                        clickChildViewWithId(R.id.buttonDelete)));

        Thread.sleep(1000);

        // Confirm deletion in dialog
        onView(withId(R.id.buttonConfirmDelete)).perform(click());
        Thread.sleep(2000);

        // Verify comment is gone
        onView(withText("Violating Policy Content")).check(doesNotExist());
    }

    // Helper to click a child view within a RecyclerView item
    public static androidx.test.espresso.ViewAction clickChildViewWithId(final int id) {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public org.hamcrest.Matcher<android.view.View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController, android.view.View view) {
                android.view.View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}
