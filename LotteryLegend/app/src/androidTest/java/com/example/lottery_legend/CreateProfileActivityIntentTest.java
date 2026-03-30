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

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.CreateProfileActivity;
import com.example.lottery_legend.entrant.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateProfileActivityIntentTest {

    @Rule
    public ActivityScenarioRule<CreateProfileActivity> scenario =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), CreateProfileActivity.class)
                    .putExtra("deviceID", "test_device_123"));

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    private void fillProfile(String name, String email) {
        onView(withId(R.id.etName)).perform(ViewActions.typeText(name), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(ViewActions.typeText(email), ViewActions.closeSoftKeyboard());
    }

    @Test
    public void testProfileEntryAndDisplay() {
        fillProfile("Edmonton User", "edmonton@example.com");
        onView(withText("Edmonton User")).check(matches(isDisplayed()));
        onView(withId(R.id.etName)).check(matches(withText("Edmonton User")));
    }

    @Test
    public void testSaveAndNavigation() throws InterruptedException {
        fillProfile("Nav Test", "nav@test.com");

        onView(withId(R.id.btnSave)).perform(click());
        Thread.sleep(2000);

        intended(hasComponent(MainActivity.class.getName()));
        intended(hasExtra("deviceId", "test_device_123"));
    }
}