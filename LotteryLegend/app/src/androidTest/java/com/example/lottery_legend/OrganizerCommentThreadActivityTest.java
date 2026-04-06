package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.organizer.OrganizerCommentThreadActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * UI Test for OrganizerCommentThreadActivity.
 * Uses Firestore Emulator to avoid direct connection to production.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerCommentThreadActivityTest {

    private static final String TAG = "OrganizerCommentThreadActivityTest";
    private static final String TEST_EVENT_ID = "test-event-id-organizer";
    private static final String TEST_PARENT_COMMENT_ID = "test-parent-comment-id-organizer";
    private static final String TEST_REPLY_ID = "test-reply-id-organizer";
    private static final String TEST_DEVICE_ID = "test-device-id-organizer";
    private FirebaseFirestore db;

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerCommentThreadActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("parentCommentId", TEST_PARENT_COMMENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        intent.putExtra("currentUserName", "Organizer User");
        intent.putExtra("currentUserType", "ORGANIZER");
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        try {
            db.useEmulator("10.0.2.2", 8080);
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (IllegalStateException e) {
            // Already configured
        }

        // Setup a parent comment in Firestore
        Comment parentComment = new Comment();
        parentComment.setCommentId(TEST_PARENT_COMMENT_ID);
        parentComment.setAuthorId("some-entrant-id");
        parentComment.setAuthorNameSnapshot("Entrant Poster");
        parentComment.setContent("Organizer test parent comment.");
        parentComment.setCreatedAt(Timestamp.now());
        parentComment.setAuthorType("ENTRANT");

        // Setup a reply in Firestore
        Comment reply = new Comment();
        reply.setCommentId(TEST_REPLY_ID);
        reply.setAuthorId("another-entrant-id");
        reply.setAuthorNameSnapshot("Reply Entrant");
        reply.setContent("This is a reply to be deleted.");
        reply.setCreatedAt(Timestamp.now());
        reply.setAuthorType("ENTRANT");
        reply.setRootCommentId(TEST_PARENT_COMMENT_ID);
        reply.setParentCommentId(TEST_PARENT_COMMENT_ID);

        // Ensure data is written to Firestore BEFORE tests launch the activity
        try {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                    .collection("comments").document(TEST_PARENT_COMMENT_ID)
                    .set(parentComment), 20, TimeUnit.SECONDS);

            Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                    .collection("comments").document(TEST_REPLY_ID)
                    .set(reply), 20, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Setup failed: " + e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            try {
                Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                        .collection("comments").document(TEST_PARENT_COMMENT_ID)
                        .delete(), 20, TimeUnit.SECONDS);
                Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                        .collection("comments").document(TEST_REPLY_ID)
                        .delete(), 20, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "TearDown cleanup failed: " + e.getMessage());
            }
        }
    }

    @Test
    public void testUIComponentsVisible() {
        try (ActivityScenario<OrganizerCommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbarCommentThread)).check(matches(isDisplayed()));
            onView(withId(R.id.layoutParentComment)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewReplies)).check(matches(isDisplayed()));
            onView(withId(R.id.commentInputContainer)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testParentCommentDisplay() {
        try (ActivityScenario<OrganizerCommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.textParentAuthorName)).check(matches(withText("Entrant Poster")));
            onView(withId(R.id.textParentContent)).check(matches(withText("Organizer test parent comment.")));
        }
    }

    @Test
    public void testReplyInputAndSend() {
        try (ActivityScenario<OrganizerCommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            String testReply = "Organizer reply message";
            onView(withId(R.id.editTextReply)).perform(typeText(testReply), closeSoftKeyboard());
            onView(withId(R.id.buttonSendReply)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonSendReply)).perform(click());
        }
    }

    @Test
    public void testDeleteCommentDialog() {
        try (ActivityScenario<OrganizerCommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.buttonParentDelete)).perform(click());
            // Verify that the delete confirmation dialog appears
            onView(withId(R.id.textDeleteTitle)).check(matches(withText("Delete Comment")));
            onView(withId(R.id.buttonConfirmDelete)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonCancelDelete)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testReactButton() {
        try (ActivityScenario<OrganizerCommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.buttonParentReact)).perform(click());
            // Verify that the reaction dialog appears
            onView(withText("React with")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testReplyDeleteButtonVisible() {
        try (ActivityScenario<OrganizerCommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Wait for replies to load (Firestore is async)
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            
            onView(withText("This is a reply to be deleted.")).check(matches(isDisplayed()));
            // Disambiguate by matching the delete button within the specific reply item
            onView(allOf(withId(R.id.buttonReplyDelete), 
                         isDescendantOfA(allOf(withId(R.id.layoutReplyRoot), 
                                               hasDescendant(withText("Reply Entrant"))))))
                .check(matches(isDisplayed()));
        }
    }
}
