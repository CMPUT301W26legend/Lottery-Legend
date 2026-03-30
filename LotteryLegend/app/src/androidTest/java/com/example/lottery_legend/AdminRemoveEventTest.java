package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Event;
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

        Event testEvent = new Event();
        testEvent.setOrganizerId("testOrganizerId");
        testEvent.setTitle("adminEventTest");
        testEvent.setDescription("This is a test description");
        testEvent.setGeoEnabled(false);
        testEvent.setEventLocation(new Event.EventLocation("Test Location", "Test Location", 0.0, 0.0));
        testEvent.setCapacity(100);
        testEvent.setMaxWaitingList(200);
        testEvent.setEventId("adminEventTestID");
        testEvent.setStatus("open");

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
