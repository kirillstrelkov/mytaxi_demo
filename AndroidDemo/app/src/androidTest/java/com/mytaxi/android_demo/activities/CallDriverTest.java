package com.mytaxi.android_demo.activities;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.mytaxi.android_demo.R;
import com.mytaxi.android_demo.models.Driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;


// Findings and improvements
// * In side menu username has white color - very hard to read.
// * First page should be more user friendly and show at least some functionality - example map with cars - not login
// * If no internet and user tries to login no error is shown
// * When user clicks login probably better to show progress bar(if internet is slow - hard to understand if button was clicked)
// * IdlingResource doesn't work
// * Add resetting application state between runs
// * Test is unstable
// * Driver search doesn't work in landscape mode(Suggestions are not visible because keyboard blocks screen)
@RunWith(AndroidJUnit4.class)
public class CallDriverTest {
    // Data, should be defined in another place for reuse
    private static final String USERNAME = "crazydog335";
    private static final String PASSWORD = "venture";

    // IDs of the UI elements, should be defined in another place for reuse
    private static final int FIELD_PASSWORD = R.id.edt_password;
    private static final int FIELD_USERNAME = R.id.edt_username;
    private static final int BTN_LOGIN = R.id.btn_login;
    private static final int FIELD_SEARCH = R.id.textSearch;
    private static final int BTN_FLOAT_ACTION = R.id.fab;

    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant("android.permission.CALL_PHONE");

    private IdlingResource mIdlingResource;

    //  Wrappers for common methods for better readability
    private void type(int id, String text) {
        onView(withId(id)).perform(typeText(text), closeSoftKeyboard());
        sleep();
    }

    private void click(int id) {
        onView(withId(id)).perform(ViewActions.click());
        sleep();
    }

    // Custom asserts
    private void assertTextIsDisplayed(String text) {
        onView(withText(containsString(text))).check(matches(isDisplayed()));
    }

    private void assertDialerIsOpenedWith(String phone) {
        intended(allOf(hasAction(Intent.ACTION_DIAL),
                hasData("tel:" + phone),
                toPackage("com.google.android.dialer")));
    }

    // Additional methods
    private void sleep() {
        // TODO: This sleep should be changed to use IdlingResource
        // find a proper way to IdlingResource with multiple Activities
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Driver selectDriver(int position) {
        Driver driver = activityRule.getActivity().getDriver(position);
        String selectedDriverName = driver.getName();
        onView(withText(containsString(selectedDriverName)))
                .inRoot(isPlatformPopup())
                .perform(ViewActions.click());
        sleep();
        return driver;
    }

    public void login() {
        type(FIELD_USERNAME, USERNAME);
        type(FIELD_PASSWORD, PASSWORD);
        click(BTN_LOGIN);

        sleep();
    }

    public void safeLogout() {
        try {
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
            onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_logout));
        } catch (Exception e) {
        }
    }

    @Rule
    public IntentsTestRule<MainActivity> activityRule =
            new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mIdlingResource = activityRule.getActivity().getIdlingResource();
        Espresso.registerIdlingResources(mIdlingResource);

        safeLogout();
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            Espresso.unregisterIdlingResources(mIdlingResource);
        }
    }

    @Test
    public void searchAndCallDriver() {
        final String searchText = "sa";
        final int driverToSelect = 1;

        login();
        assertTextIsDisplayed("mytaxi demo");

        type(FIELD_SEARCH, searchText);
        Driver driver = selectDriver(driverToSelect);
        assertTextIsDisplayed("Driver Profile");
        assertTextIsDisplayed(driver.getName());

        click(BTN_FLOAT_ACTION);
        assertDialerIsOpenedWith(driver.getPhone());
    }
}
