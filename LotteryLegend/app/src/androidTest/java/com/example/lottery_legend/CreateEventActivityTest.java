package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.equalTo;

import android.content.Context;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.organizer.CreateEventActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {

    private static final String TEST_DEVICE_ID = "test_device_123";

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, CreateEventActivity.class);
        intent.putExtra("deviceId", TEST_DEVICE_ID);
        return intent;
    }

    @Test
    public void testCreateEventActivityLaunch() {
        onView(withId(R.id.editTextEventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.createButton)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void testFillFormAndSubmit() {
        onView(withId(R.id.editTextEventTitle))
                .perform(typeText("UI Test Event"), closeSoftKeyboard());
        onView(withId(R.id.editTextDescription))
                .perform(typeText("This is an automated test description."), closeSoftKeyboard());

        onView(withId(R.id.editTextPrice)).perform(scrollTo(), typeText("10.0"), closeSoftKeyboard());
        onView(withId(R.id.editTextLocation)).perform(scrollTo(), typeText("Test Location"), closeSoftKeyboard());

        setDate(R.id.eventStartDateTime, 2025, 12, 1);
        setDate(R.id.eventEndDateTime, 2025, 12, 2);

        setDate(R.id.registrationStartDateTime, 2025, 11, 1);
        setDate(R.id.registrationEndDateTime, 2025, 11, 30);
        setDate(R.id.drawDateTime, 2025, 12, 1);

        onView(withId(R.id.Capacity)).perform(scrollTo(), typeText("50"), closeSoftKeyboard());

        onView(withId(R.id.editTextEventTitle)).check(matches(withText("UI Test Event")));

        onView(withId(R.id.createButton)).perform(scrollTo(), click());
    }

    private void setDate(int viewId, int year, int month, int day) {
        onView(withId(viewId)).perform(scrollTo(), click());
        
        // Handle DatePickerDialog
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(year, month, day));
        onView(withId(android.R.id.button1)).perform(click());
        
        // Handle TimePickerDialog (it opens automatically after DatePicker)
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(12, 0));
        onView(withId(android.R.id.button1)).perform(click());
    }
}
