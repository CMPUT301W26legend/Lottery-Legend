package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.organizer.OrganizerCommentsActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * UI Test for OrganizerCommentsActivity.
 * Uses Firestore Emulator to avoid direct connection to production.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerCommentsActivityTest {

    private static final String TEST_EVENT_ID = "test-event-comments-id";
    private static final String TEST_DEVICE_ID = "test-org-id-comments";
    private static final String TEST_COMMENT_ID = "test-parent-comment-1";
    private FirebaseFirestore db;

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerCommentsActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        intent.putExtra("authorName", "Organizer Test");
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
        Comment comment = new Comment();
        comment.setCommentId(TEST_COMMENT_ID);
        comment.setAuthorId("entrant-id-123");
        comment.setAuthorNameSnapshot("Entrant Name");
        comment.setContent("Hello Organizer, I have a question.");
        comment.setCreatedAt(Timestamp.now());
        comment.setAuthorType("ENTRANT");
        comment.setThreadLevel(0);

        Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                .collection("comments").document(TEST_COMMENT_ID)
                .set(comment), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                    .collection("comments").document(TEST_COMMENT_ID)
                    .delete(), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testUIComponentsVisible() {
        try (ActivityScenario<OrganizerCommentsActivity> scenario = ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbarComments)).check(matches(isDisplayed()));
            onView(withId(R.id.textToolbarTitle)).check(matches(withText("Comments")));
            onView(withId(R.id.recyclerViewComments)).check(matches(isDisplayed()));
            onView(withId(R.id.commentInputContainer)).check(matches(isDisplayed()));
            onView(withId(R.id.navbar)).check(matches(isDisplayed()));
        }
    }

    /**
     * Helper matcher to pick a view at a specific index when multiple views match.
     */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
