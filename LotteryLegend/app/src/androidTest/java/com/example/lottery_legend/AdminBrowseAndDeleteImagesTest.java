package com.example.lottery_legend;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.view.View;

import com.example.lottery_legend.admin.AdminActivity;
import com.example.lottery_legend.model.Event;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Test for US 03.06.01: Browsing and removing images as an administrator.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminBrowseAndDeleteImagesTest {

    private FirebaseFirestore db;
    private final String TEST_POSTER = "iVBORw0KGgoAAAANSUhEUgAAAOEAAADhCAMAAAAJbSJIAAAAb1BMVEUAAAD///99fX2MjIybm5v29varq6u4uLjIyMjn5+fX19f5+fnv7+/r6+vV1dXy8vLBwcEjIyNWVlbg4OCpqamSkpLExMQ0NDRubm5hYWGZmZmHh4cODg5paWkvLy95eXlLS0snJydPT08+Pj4VFRUnfKLvAAADnklEQVR4nO3d6XKiQBiF4W4QodkEQeMWozH3f43TSjQuTDKTsqvrfJz3Z6pSnKdcsDQRpXvLbNGliTGmSdMysc3jU+NLo65VeFvwn63vfn81utRejnU6dGVXlKnNrsovG+3gfop6/FFk0nZ52NYbBdRH/TJdlSb6B2ExDw++5/667Xpe/CDMxwuom+6hzWJcfCdMFr4XPqHX6q/CaPTie91T2q6ifmEe+J72tN6KPmGz9L3riU2bR2Ez9b3qqe3Se6EwoCU2t8Jc0l20a1pcCyM5TzJfvU2uhCPfa5w0+xLOZZwH79tWZ2Eu4ZVMX6/Fp3Dse4mz2k5YSL0J7Y1oTsLkw/cQd1VHYRT6nuGwYGKFRuYTade+scLS9wqnJVYo82x/LtQqk/aS+7ZdpjLJD0OlaiusfY9w2iZSEfZ7az9mhb4nOI5C/CjET74wFy801ii7xhpll1IIX2mNsksohC9Rst+mUaqyRtnFFMIX2/up7MbWKDsK8ZMvbJXcD4C7RhTCRyF+QxC2vic4bqZkf8itVEghfBTiRyF+FOIX2nO+7NZK8l+XHgsohI9C/IYgXPue4LhASfyvtesoxI9C/CjEj0L8KMSPQvwoxI9C/CjEj0L8KMSPQvwoxI9C/CjEj0L8KMSPQvwoxI9C/CjEj0L8KMSPQvwoxI9C/CjEj0L8KMSPQvwoxI9C/CjEj0L8KMSPQvwoxI9C/CjEj0L8KMSPQvwoxI9C/CjEbwjfTyP/O4YoRI9C/IYglP/dl/K/v5RC9CjEj0L8KMQvHMA1SuRfZ4ZC9CjEbwhC+dc/lC6Ufw3LIQjlXw9Y/jWdKUQvVqXvCY6rVOp7guMSCuFLlPE9wXGleGE6AGHue4LjGhX5nuA4I16YixdGFMJHIX5WuPG9wW2RymrfG9xmhS++NzitzlQ29T3CabtM6ZXvEU4LtdKy36hJrNBsfa9wWN1YYST5r/WDiRXqueAzYqWPwmLhe4ezluYk1HI/Bm51JyxefS9x1PEmPAl1JfPpdB/rs1DoWT/UX8LozfcaBy3yK6Eu5L06fTf6Wqibne9FT+6Q6luhTne+Nz21Q6nvhbqRdEd9T/WjUBdynm4WRvcJ9WQm47y4D3PdL7SnfgmvbpbxjelWqIsW3bhsjf5OqLWpgr3vlb+uDipzD3oQ2odjk4Q7vPcY612YNJNHTo/wWJZFXblp0rRMkqSK43hsa0enZuG5deCy9eU4s+7A7XGE3VLZTWWaNib/XJpl/ZQ/Z6Qe8ZdxnRMAAAAASUVORK5CYII=";

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();

        Event testEvent = new Event();
        testEvent.setEventId("eventPosterTestID");
        testEvent.setOrganizerId("testOrganizerId");
        testEvent.setTitle("eventTestNameMedia");
        testEvent.setDescription("Description for visible test");
        testEvent.setGeoEnabled(false);
        testEvent.setEventLocation(new Event.EventLocation("Test Location", null, null, null));
        testEvent.setCapacity(100);
        testEvent.setMaxWaitingList(200);
        testEvent.setPosterImage(TEST_POSTER);
        testEvent.setStatus("open");
        
        testEvent.setCreatedAt(Timestamp.now());
        testEvent.setUpdatedAt(Timestamp.now());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        testEvent.setEventStartAt(new Timestamp(sdf.parse("2026-01-01")));
        testEvent.setEventEndAt(new Timestamp(sdf.parse("2026-01-02")));
        testEvent.setRegistrationStartAt(new Timestamp(sdf.parse("2025-12-01")));
        testEvent.setRegistrationEndAt(new Timestamp(sdf.parse("2025-12-31")));
        testEvent.setDrawAt(new Timestamp(sdf.parse("2026-01-01")));

        Tasks.await(db.collection("events").document("eventPosterTestID").set(testEvent), 10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            db.collection("events").document("eventPosterTestID").delete();
        }
    }

    @Test
    public void testBrowseAndRemoveVisibleImage() throws InterruptedException {
        onView(withId(R.id.nav_admin_media)).perform(click());
        Thread.sleep(1000);
        
        onView(withId(R.id.admin_media_recycler))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(allOf(withId(R.id.media_event_title), withText("eventTestNameMedia")))));

        onView(allOf(withId(R.id.media_event_title), withText("eventTestNameMedia"))).check(matches(isDisplayed()));

        onView(withId(R.id.search_bar_media)).perform(typeText("eventTestNameMedia"), closeSoftKeyboard());
        Thread.sleep(1000);
        
        onView(allOf(withId(R.id.media_event_title), withText("eventTestNameMedia"))).check(matches(isDisplayed()));

        onView(withId(R.id.admin_media_recycler))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(allOf(withId(R.id.media_event_title), withText("eventTestNameMedia"))),
                        clickOnViewChild(R.id.btn_remove_media)));

        Thread.sleep(1000);
        onView(withText("Delete Image")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete)).perform(click());
        Thread.sleep(1000);
        
        onView(allOf(withId(R.id.media_event_title), withText("eventTestNameMedia"))).check(doesNotExist());
    }

    /**
     * Helper to click on a specific view inside a RecyclerView item.
     */
    public static ViewAction clickOnViewChild(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }
}
