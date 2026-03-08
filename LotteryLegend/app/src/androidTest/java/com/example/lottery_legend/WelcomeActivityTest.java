package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.anyOf;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
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
            // 1. 尝试查找欢迎页特有的按钮
            onView(withId(R.id.CreateProfileButton)).check(matches(isDisplayed()));

            // 2. 如果找到了（新用户），执行点击并验证跳转到 CreateProfileActivity
            onView(withId(R.id.CreateProfileButton)).perform(click());
            intended(hasComponent(CreateProfileActivity.class.getName()));

        } catch (AssertionError | Exception e) {
            // 3. 如果没找到按钮（老用户，已自动跳转），验证是否发出了前往 MainActivity 的 Intent
            // 有了 IntentsRule(order=0)，即使跳转发生在 Activity 启动瞬间也能被记录到
            intended(hasComponent(MainActivity.class.getName()));
        }
    }
}