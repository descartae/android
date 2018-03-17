package org.descartae.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.descartae.android.view.activities.IntroActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IntroActivityInstrumentedTest {

    @Rule
    public ActivityTestRule<IntroActivity> menuActivityTestRule = new ActivityTestRule<>(IntroActivity.class, true, true);

    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.descartae", appContext.getPackageName());
    }

    @Test
    public void testStartButton() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        onView(withId(R.id.pager)).perform(swipeRight()).perform(swipeRight()).perform(swipeRight());
        onView(withId(R.id.button_start)).perform(click());
        onView(withText(appContext.getString(R.string.permission_gps_title))).check(matches(isDisplayed()));
    }

    @Test
    public void testItemsVisibility() throws Exception {
        onView(withId(R.id.textView_title)).check(matches(isDisplayed()));
        onView(withId(R.id.textView_subtitle)).check(matches(isDisplayed()));
        onView(withId(R.id.imageView_intro)).check(matches(isDisplayed()));
    }
}
