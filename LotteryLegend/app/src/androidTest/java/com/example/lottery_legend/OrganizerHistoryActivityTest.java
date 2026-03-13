package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerHistoryActivityTest {

    @Rule
    public ActivityScenarioRule<OrganizerHistoryActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerHistoryActivity.class);
        intent.putExtra("deviceId", "test-device-id");
        return intent;
    }

    @Test
    public void testOrganizerHistoryActivityLaunch() {
        onView(withId(R.id.recyclerOrganizerEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
    }
}
