package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery_legend.entrant.CreateProfileActivity;
import com.example.lottery_legend.entrant.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateProfileActivityIntentTest {

    private static final String TEST_DEVICE_ID = "test_device_123";

    @Before
    public void setUp() {
        Intents.init();

        // Optional: Firestore emulator setup (only works if emulator is running)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            db.useEmulator("10.0.2.2", 8080);
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (IllegalStateException e) {
            // արդեն configured
        }
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    private void fillProfile(String name, String email, String phone) {
        onView(withId(R.id.etName)).perform(typeText(name), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.etPhone)).perform(typeText(phone), closeSoftKeyboard());
    }

    private Intent createStartIntent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                CreateProfileActivity.class
        );

        // ✅ FIXED KEY HERE
        intent.putExtra("deviceId", TEST_DEVICE_ID);

        return intent;
    }

    @Test
    public void testProfileEntryAndDisplay() {
        try (ActivityScenario<CreateProfileActivity> scenario =
                     ActivityScenario.launch(createStartIntent())) {

            fillProfile("Edmonton User", "edmonton@example.com", "780-123-4567");

            onView(withId(R.id.etName))
                    .check(matches(withText("Edmonton User")));
        }
    }
}