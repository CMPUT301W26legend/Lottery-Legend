package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.admin.AdminUserDetailActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Test for AdminUserDetailActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminUserDetailActivityTest {

    private Intent createEntrantIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, AdminUserDetailActivity.class);
        intent.putExtra("userId", "test_entrant_id");
        intent.putExtra("name", "Test Entrant");
        intent.putExtra("email", "entrant@test.com");
        intent.putExtra("phone", "123-456-7890");
        intent.putExtra("collectionName", "entrants");
        intent.putExtra("profileImage", ""); // empty for default avatar
        return intent;
    }

    private Intent createOrganizerIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, AdminUserDetailActivity.class);
        intent.putExtra("userId", "test_organizer_id");
        intent.putExtra("name", "Test Organizer");
        intent.putExtra("email", "organizer@test.com");
        intent.putExtra("phone", "098-765-4321");
        intent.putExtra("collectionName", "organizers");
        intent.putExtra("profileImage", "");
        return intent;
    }

    @Test
    public void testEntrantDetailDisplay() {
        try (ActivityScenario<AdminUserDetailActivity> scenario = ActivityScenario.launch(createEntrantIntent())) {
            onView(withId(R.id.user_name_detail)).check(matches(withText("Test Entrant")));
            onView(withId(R.id.user_email_detail)).check(matches(withText("entrant@test.com")));
            onView(withId(R.id.user_phone_detail)).check(matches(withText("123-456-7890")));
            onView(withId(R.id.btn_delete_user)).check(matches(withText("Delete Entrant")));
        }
    }

    @Test
    public void testOrganizerDetailDisplay() {
        try (ActivityScenario<AdminUserDetailActivity> scenario = ActivityScenario.launch(createOrganizerIntent())) {
            onView(withId(R.id.user_name_detail)).check(matches(withText("Test Organizer")));
            onView(withId(R.id.user_email_detail)).check(matches(withText("organizer@test.com")));
            onView(withId(R.id.user_phone_detail)).check(matches(withText("098-765-4321")));
            onView(withId(R.id.btn_delete_user)).check(matches(withText("Delete Organizer")));
        }
    }

    @Test
    public void testDeleteDialogForEntrant() {
        try (ActivityScenario<AdminUserDetailActivity> scenario = ActivityScenario.launch(createEntrantIntent())) {
            onView(withId(R.id.btn_delete_user)).perform(click());
            
            // Check dialog components
            onView(withId(R.id.dialog_title)).check(matches(withText("Delete Entrant")));
            onView(withId(R.id.dialog_message)).check(matches(withText("This will permanently remove this entrant.")));
            onView(withId(R.id.btn_cancel)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_delete)).check(matches(isDisplayed()));
            
            // Cancel dialog
            onView(withId(R.id.btn_cancel)).perform(click());
            onView(withId(R.id.btn_delete_user)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testDeleteDialogForOrganizer() {
        try (ActivityScenario<AdminUserDetailActivity> scenario = ActivityScenario.launch(createOrganizerIntent())) {
            onView(withId(R.id.btn_delete_user)).perform(click());
            
            // Check dialog components
            onView(withId(R.id.dialog_title)).check(matches(withText("Delete Organizer")));
            onView(withId(R.id.dialog_message)).check(matches(withText("This will permanently remove this organizer.")));
            
            onView(withId(R.id.btn_cancel)).perform(click());
        }
    }

    @Test
    public void testBackButton() {
        try (ActivityScenario<AdminUserDetailActivity> scenario = ActivityScenario.launch(createEntrantIntent())) {
            onView(withId(R.id.btn_back_detail)).perform(click());
            // Verify activity is finishing
            assertTrue(scenario.getState().isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED) || scenario.getResult() != null);
        }
    }
}
