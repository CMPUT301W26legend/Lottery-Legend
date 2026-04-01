package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.CommentsActivity;
import com.example.lottery_legend.model.Comment;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * UI tests for CommentsActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentsActivityTest {

    private static final String TAG = "CommentsActivityTest";
    private static final String TEST_EVENT_ID = "test-event-comments-id";
    private static final String TEST_DEVICE_ID = "test-entrant-device-id";
    private static final String TEST_AUTHOR_NAME = "Test User";
    private static final String TEST_COMMENT_CONTENT = "This is a test comment.";

    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<CommentsActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        intent.putExtra("authorName", TEST_AUTHOR_NAME);
        intent.putExtra("authorType", "ENTRANT");
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        // Pre-populate with one comment
        Comment comment = new Comment();
        comment.setCommentId("test-comment-id");
        comment.setAuthorId(TEST_DEVICE_ID);
        comment.setAuthorNameSnapshot(TEST_AUTHOR_NAME);
        comment.setContent(TEST_COMMENT_CONTENT);
        comment.setCreatedAt(Timestamp.now());
        comment.setThreadLevel(0);
        comment.setReactionTypeCounts(new HashMap<>());
        comment.setReplyCount(0);
        comment.setReactionCount(0);

        Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                .collection("comments").document("test-comment-id").set(comment), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            try {
                // Delete all comments in the sub-collection
                Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                        .collection("comments").get().continueWith(task -> {
                            for (var doc : task.getResult()) {
                                doc.getReference().delete();
                            }
                            return null;
                        }), 10, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "Cleanup failed: " + e.getMessage());
            }
        }
    }

    @Test
    public void testUIComponentsVisible() {
        onView(withId(R.id.toolbarComments)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerViewComments)).check(matches(isDisplayed()));
        onView(withId(R.id.commentInputContainer)).check(matches(isDisplayed()));
        onView(withId(R.id.editTextComment)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonSendComment)).check(matches(isDisplayed()));
    }

    @Test
    public void testCommentsLoaded() throws InterruptedException {
        // Wait for Firestore data
        Thread.sleep(2000);

        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(TEST_COMMENT_CONTENT))));
        onView(withText(TEST_COMMENT_CONTENT)).check(matches(isDisplayed()));
        onView(withText(TEST_AUTHOR_NAME)).check(matches(isDisplayed()));
    }

    @Test
    public void testPostComment() throws InterruptedException {
        String newComment = "New comment from UI test";
        
        onView(withId(R.id.editTextComment)).perform(typeText(newComment), closeSoftKeyboard());
        onView(withId(R.id.buttonSendComment)).perform(click());

        // Wait for Firestore roundtrip
        Thread.sleep(2000);

        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(newComment))));
        onView(withText(newComment)).check(matches(isDisplayed()));
    }

    @Test
    public void testReplyInteraction() throws InterruptedException {
        Thread.sleep(2000);

        // Click Reply button on the pre-populated comment
        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_COMMENT_CONTENT)), 
                        clickOnViewChild(R.id.buttonReply)));

        // Verify EditText hint changes to reflect reply mode
        onView(withId(R.id.editTextComment)).check(matches(withHint("Reply to " + TEST_AUTHOR_NAME + "...")));
    }

    @Test
    public void testReactionDialog() throws InterruptedException {
        Thread.sleep(2000);

        // Click React button on the first comment
        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_COMMENT_CONTENT)), 
                        clickOnViewChild(R.id.buttonReact)));

        // Verify dialog shows options
        onView(withText("LIKE 👍")).check(matches(isDisplayed()));
        onView(withText("LOVE ❤️")).check(matches(isDisplayed()));
        onView(withText("HELPFUL ⭐")).check(matches(isDisplayed()));
    }

    @Test
    public void testDeleteComment() throws InterruptedException {
        Thread.sleep(2000);

        // Long click on the pre-populated comment to trigger deletion (since we are the author)
        onView(withId(R.id.recyclerViewComments))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_COMMENT_CONTENT)), 
                        longClick()));

        // Wait for deletion to reflect
        Thread.sleep(2000);

        onView(withText(TEST_COMMENT_CONTENT)).check(doesNotExist());
    }

    /**
     * Helper to click on a specific view inside a RecyclerView item.
     */
    public static ViewAction clickOnViewChild(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }
}
