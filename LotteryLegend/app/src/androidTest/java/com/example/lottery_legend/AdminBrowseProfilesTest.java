package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Test for US 03.05.01: Browsing user profiles as an administrator.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminBrowseProfilesTest {
    
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        Timestamp now = new Timestamp(new Date());

        Entrant testEntrant = new Entrant(
                "testEntrant",
                "entrant@test.com",
                "1234567890",
                true,
                "testEntrantID",
                now
        );
        Tasks.await(db.collection("entrants").document("testEntrantID").set(testEntrant), 5, TimeUnit.SECONDS);

        Organizer testOrganizer = new Organizer(
                "testOrganizerID",
                "testOrganizer",
                "organizer@test.com",
                "0987654321",
                now,
                now,
                false,
                new ArrayList<>()
        );
        Tasks.await(db.collection("organizers").document("testOrganizerID").set(testOrganizer), 5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            Tasks.await(db.collection("entrants").document("testEntrantID").delete(), 5, TimeUnit.SECONDS);
            Tasks.await(db.collection("organizers").document("testOrganizerID").delete(), 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testNavigateToUsersAndCheckTabs() {
        onView(withId(R.id.nav_admin_users)).perform(click());

        onView(withId(R.id.admin_users_tabs)).check(matches(isDisplayed()));
        onView(withText("Entrants")).check(matches(isDisplayed()));
        onView(withText("Organizers")).check(matches(isDisplayed()));
        onView(withId(R.id.admin_users_recycler)).check(matches(isDisplayed()));
    }

    @Test
    public void testTestUsersAreDisplayed() throws InterruptedException {
        onView(withId(R.id.nav_admin_users)).perform(click());

        onView(withText("Entrants")).perform(click());
        onView(withId(R.id.admin_users_recycler))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("testEntrant"))));
        onView(withText("testEntrant")).check(matches(isDisplayed()));

        onView(withText("Organizers")).perform(click());
        onView(withId(R.id.admin_users_recycler))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("testOrganizer"))));
        onView(withText("testOrganizer")).check(matches(isDisplayed()));
    }

    @Test
    public void testSwitchBetweenUserTabs() {
        onView(withId(R.id.nav_admin_users)).perform(click());

        onView(withText("Organizers")).perform(click());
        onView(withId(R.id.admin_users_recycler)).check(matches(isDisplayed()));

        onView(withText("Entrants")).perform(click());
        onView(withId(R.id.admin_users_recycler)).check(matches(isDisplayed()));
    }
}
