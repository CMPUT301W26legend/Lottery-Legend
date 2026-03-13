package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerEventDetailsActivityTest {

    @Rule
    public ActivityScenarioRule<OrganizerEventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerEventDetailsActivity.class);

    @Test
    public void testOrganizerEventDetailsActivityLaunch() {
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }
}
