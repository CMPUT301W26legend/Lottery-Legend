package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.MainActivity;
import com.example.lottery_legend.entrant.ProfileActivity;
import com.example.lottery_legend.event.WaitingListDialogFragment;
import com.example.lottery_legend.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * UI tests for MainActivity, including dialog interactions and navigation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    private static final String TEST_DEVICE_ID = "test-device-id";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testUIComponentsVisible() {
        onView(withId(R.id.eventView)).check(matches(isDisplayed()));
        onView(withId(R.id.navbar)).check(matches(isDisplayed()));
        onView(withId(R.id.navHome)).check(matches(isDisplayed()));
        onView(withId(R.id.navProfile)).check(matches(isDisplayed()));
    }

    @Test
    public void testJoinWaitingListDialogUI() {
        Event openEvent = createMockEvent("Open Event", "open");
        
        activityRule.getScenario().onActivity(activity -> {
            WaitingListDialogFragment dialog = WaitingListDialogFragment.newInstance(openEvent, TEST_DEVICE_ID);
            dialog.show(activity.getSupportFragmentManager(), "join_test");
        });

        // Use inRoot(isDialog()) to explicitly target the dialog window, 
        // which helps avoid RootViewWithoutFocusException on the activity window.
        onView(withText("Open Event"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        
        onView(withId(R.id.btnConfirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        
        onView(withId(R.id.btnCancel))
                .inRoot(isDialog())
                .perform(click());
    }

    @Test
    public void testLeaveWaitingListDialogUI() {
        Event joinedEvent = createMockEvent("Joined Event", "open");
        List<Event.WaitingListEntry> waitingList = new ArrayList<>();
        Event.WaitingListEntry entry = new Event.WaitingListEntry();
        entry.setDeviceId(TEST_DEVICE_ID);
        waitingList.add(entry); 
        joinedEvent.setWaitingList(waitingList);

        activityRule.getScenario().onActivity(activity -> {
            WaitingListDialogFragment dialog = WaitingListDialogFragment.newInstance(joinedEvent, TEST_DEVICE_ID);
            dialog.show(activity.getSupportFragmentManager(), "leave_test");
        });

        onView(withText("Leave Waiting List"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        
        onView(withText("Do you want to leave this waiting list?"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        
        onView(withId(R.id.btnConfirm))
                .inRoot(isDialog())
                .check(matches(withText("Leave")));
        
        onView(withId(R.id.btnCancel))
                .inRoot(isDialog())
                .perform(click());
    }

    @Test
    public void testNavigationToProfile() {
        onView(withId(R.id.navProfile)).perform(click());
        intended(allOf(
                hasComponent(ProfileActivity.class.getName()),
                hasExtra("deviceId", TEST_DEVICE_ID)
        ));
    }

    private Event createMockEvent(String title, String status) {
        Event event = new Event();
        event.setOrganizerId("org1");
        event.setTitle(title);
        event.setDescription("Desc");
        event.setGeoEnabled(true);
        event.setEventLocation(new Event.EventLocation("Loc", "Loc", 0.0, 0.0));
        event.setCapacity(10);
        event.setMaxWaitingList(20);
        event.setEventId("test_id_" + title);
        event.setStatus(status);
        event.setWaitingList(new ArrayList<>());
        return event;
    }
}
