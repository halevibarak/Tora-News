package com.barak.tabs.ui;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Barak Halevi on 09/12/2018.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkAppRaterDisplayed() {
        activityTestRule.launchActivity(new Intent());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {


        }
        onView(withText("אם נהנית מהאפליקציה, אשמח לקבל דירוג טוב בחנות")).check(matches(isDisplayed()));
    }
}

