package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

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

        onView(withText("Open Event")).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).check(matches(withText("Confirm"))); // 假设默认是 Confirm
        onView(withId(R.id.btnCancel)).perform(click());
    }

    @Test
    public void testLeaveWaitingListDialogUI() {
        Event joinedEvent = createMockEvent("Joined Event", "open");
        List<String> waitingList = new ArrayList<>();
        waitingList.add(TEST_DEVICE_ID); // 模拟用户已加入
        joinedEvent.setWaitingList(waitingList);

        activityRule.getScenario().onActivity(activity -> {
            WaitingListDialogFragment dialog = WaitingListDialogFragment.newInstance(joinedEvent, TEST_DEVICE_ID);
            dialog.show(activity.getSupportFragmentManager(), "leave_test");
        });

        onView(withText("Leave Waiting List")).check(matches(isDisplayed()));
        onView(withText("Do you want to leave this waiting list?")).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).check(matches(withText("Leave")));
        
        onView(withId(R.id.btnCancel)).perform(click());
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
        Event event = new Event("org1", title, "Desc", true, "Loc",
                "2023-10-01", "2023-10-02", "2023-09-01", "2023-09-30", "2023-10-01", 10, 20);
        event.setEventId("test_id_" + title);
        event.setStatus(status);
        event.setWaitingList(new ArrayList<>());
        return event;
    }
}
