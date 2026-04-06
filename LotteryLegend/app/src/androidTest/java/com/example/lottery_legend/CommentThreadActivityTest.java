package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.CommentThreadActivity;
import com.example.lottery_legend.model.Comment;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * UI Test for CommentThreadActivity.
 * Uses manual ActivityScenario launch to ensure Firestore data is ready before activity starts.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentThreadActivityTest {

    private static final String TEST_EVENT_ID = "test-event-id";
    private static final String TEST_PARENT_COMMENT_ID = "test-parent-comment-id";
    private static final String TEST_DEVICE_ID = "test-device-id";
    private FirebaseFirestore db;

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, CommentThreadActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("parentCommentId", TEST_PARENT_COMMENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        intent.putExtra("currentUserName", "Test User");
        intent.putExtra("currentUserType", "ENTRANT");
        intent.putExtra("isAdmin", false);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Setup a parent comment in Firestore
        Comment parentComment = new Comment();
        parentComment.setCommentId(TEST_PARENT_COMMENT_ID);
        parentComment.setAuthorId("other-author-id");
        parentComment.setAuthorNameSnapshot("Original Poster");
        parentComment.setContent("This is the main comment content.");
        parentComment.setCreatedAt(Timestamp.now());
        parentComment.setAuthorType("ENTRANT");

        // Ensure data is written to Firestore BEFORE tests launch the activity
        Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                .collection("comments").document(TEST_PARENT_COMMENT_ID)
                .set(parentComment), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                    .collection("comments").document(TEST_PARENT_COMMENT_ID)
                    .delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testUIComponentsVisible() {
        try (ActivityScenario<CommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbarCommentThread)).check(matches(isDisplayed()));
            onView(withId(R.id.layoutParentComment)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewReplies)).check(matches(isDisplayed()));
            onView(withId(R.id.commentInputContainer)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testParentCommentDisplay() {
        try (ActivityScenario<CommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            // Check that the author name and content are displayed correctly
            onView(withId(R.id.textParentAuthorName)).check(matches(withText("Original Poster")));
            onView(withId(R.id.textParentContent)).check(matches(withText("This is the main comment content.")));
        }
    }

    @Test
    public void testReplyInputAndSend() {
        try (ActivityScenario<CommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            String testReply = "Test reply message";
            onView(withId(R.id.editTextReply)).perform(typeText(testReply), closeSoftKeyboard());
            onView(withId(R.id.buttonSendReply)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonSendReply)).perform(click());
        }
    }

    @Test
    public void testReplyToParentButton() {
        try (ActivityScenario<CommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.buttonParentReply)).perform(click());
            onView(withId(R.id.editTextReply)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testReactButton() {
        try (ActivityScenario<CommentThreadActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.buttonParentReact)).perform(click());
            // Verify that the reaction dialog appears
            onView(withText("React with")).check(matches(isDisplayed()));
        }
    }
}
