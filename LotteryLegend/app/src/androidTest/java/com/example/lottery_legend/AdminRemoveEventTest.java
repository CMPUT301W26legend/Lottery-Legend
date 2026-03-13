package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Test for US 03.01.01: Removing events as an administrator.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminRemoveEventTest {

    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        Event testEvent = new Event(
                "testOrganizerId",
                "adminEventTest",
                "This is a test description",
                false,
                "Test Location",
                "2026-01-01",
                "2026-01-02",
                "2025-12-01",
                "2025-12-31",
                "2026-01-01",
                100,
                200
        );
        testEvent.setEventId("adminEventTestID");

        Tasks.await(db.collection("events").document("adminEventTestID").set(testEvent), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            db.collection("events").document("adminEventTestID").delete();
        }
    }

    @Test
    public void testRemoveEvent() throws InterruptedException {
        onView(withId(R.id.nav_admin_events)).perform(click());
        onView(withId(R.id.admin_events_recycler))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("adminEventTest")), click()));
        onView(withId(R.id.btn_delete_event_admin)).perform(click());

        onView(withId(R.id.btn_delete)).perform(click());
        onView(withText("adminEventTest")).check(doesNotExist());
    }
}
