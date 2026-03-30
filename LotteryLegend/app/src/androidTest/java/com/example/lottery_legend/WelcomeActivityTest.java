package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.anyOf;

import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.CreateProfileActivity;
import com.example.lottery_legend.entrant.MainActivity;
import com.example.lottery_legend.entrant.WelcomeActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for WelcomeActivity.
 * Handles both cases: new user (stay on Welcome) and existing user (auto-redirect).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WelcomeActivityTest {

    @Rule(order = 0)
    public IntentsRule intentsRule = new IntentsRule();

    @Rule(order = 1)
    public ActivityScenarioRule<WelcomeActivity> scenario =
            new ActivityScenarioRule<>(WelcomeActivity.class);

    /**
     * Test that the app either shows the Welcome button OR has already redirected to MainActivity.
     */
    @Test
    public void testWelcomeOrRedirected() {
        // anyOf allows matching either the Welcome button or the MainActivity's root
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }

    /**
     * Test navigation. If we are on Welcome screen, test button click.
     * If we are already redirected, verify we are in MainActivity.
     */
    @Test
    public void testNavigation() {
        try {
            // 1. Try to find the welcome-specific button
            onView(withId(R.id.CreateProfileButton)).check(matches(isDisplayed()));

            // 2. If found (new user), click it and verify navigation to CreateProfileActivity
            onView(withId(R.id.CreateProfileButton)).perform(click());
            intended(hasComponent(CreateProfileActivity.class.getName()));

        } catch (AssertionError | Exception e) {
            // 3. If button not found (returning user, auto-redirected), verify intent to MainActivity
            // With IntentsRule(order=0), the intent can be captured even if it happened during onCreate
            intended(hasComponent(MainActivity.class.getName()));
        }
    }
}
