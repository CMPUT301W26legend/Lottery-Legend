package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.CreateProfileActivity;
import com.example.lottery_legend.entrant.MainActivity;
import com.example.lottery_legend.entrant.WelcomeActivity;
import com.example.lottery_legend.organizer.OrganizerMainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for WelcomeActivity.
 * Handles both cases: new user (stay on Welcome) and existing user (WelcomeExist).
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
     * Test that the app shows the root layout after loading.
     */
    @Test
    public void testWelcomeOrRedirected() throws InterruptedException {
        // Wait for Firebase to return and setContentView to be called
        Thread.sleep(2000);
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }

    /**
     * Test navigation. If we are on Welcome screen, test button click.
     * If we are on WelcomeExist screen, test continue button click.
     */
    @Test
    public void testNavigation() throws InterruptedException {
        // Wait for Firebase to return and setContentView to be called
        Thread.sleep(2000);
        
        try {
            // 1. Try to find the welcome-specific button (New User)
            onView(withId(R.id.CreateProfileButton)).check(matches(isDisplayed()));

            // 2. If found, click it and verify navigation to CreateProfileActivity
            onView(withId(R.id.CreateProfileButton)).perform(click());
            intended(hasComponent(CreateProfileActivity.class.getName()));

        } catch (AssertionError | Exception e) {
            // 3. If button not found, we are likely on the WelcomeExist screen (Existing User)
            onView(withId(R.id.btnContinue)).check(matches(isDisplayed()));
            onView(withId(R.id.btnContinue)).perform(click());
            
            // Wait for intent (another Firebase fetch inside click listener)
            Thread.sleep(2000);
            
            // Could go to either MainActivity or OrganizerMainActivity
            try {
                intended(hasComponent(MainActivity.class.getName()));
            } catch (AssertionError ae) {
                intended(hasComponent(OrganizerMainActivity.class.getName()));
            }
        }
    }
}
