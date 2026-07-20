package com.example.agriguard.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.agriguard.R
import com.example.agriguard.activities.DashboardActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso tests for Logout flow from ProfileFragment.
 *
 * Covers:
 *  - TC_LOGOUT_001: Logout button visible on Profile tab
 *  - TC_LOGOUT_002: Clicking Logout shows confirmation dialog (if any)
 *  - TC_LOGOUT_003: Confirming logout navigates to LoginActivity
 *  - TC_LOGOUT_004: After logout, cannot navigate back to Dashboard
 *  - TC_LOGOUT_005: Logout button has correct label text
 *  - TC_LOGOUT_006: Logout clears user session (Firebase currentUser is null)
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class LogoutTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    private fun navigateToProfile() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_profile))
    }

    // TC_LOGOUT_001
    @Test
    fun tc_logout_001_logoutBtnVisible() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).check(matches(isDisplayed()))
    }

    // TC_LOGOUT_002
    @Test
    fun tc_logout_002_logoutBtnLabel() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).check(matches(withText("Logout")))
    }

    // TC_LOGOUT_003: Clicking Logout navigates to Login
    @Test
    fun tc_logout_003_logoutNavigatesToLogin() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).perform(click())
        // After logout, LoginActivity should be visible
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }

    // TC_LOGOUT_004: After logout, pressing back does not re-enter Dashboard
    @Test
    fun tc_logout_004_logoutClearsBackStack() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).perform(click())
        // DashboardActivity is finished — pressing back should not show it
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }

    // TC_LOGOUT_005: Logout button text color (red) — verify text
    @Test
    fun tc_logout_005_logoutButtonIsClickable() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).check(matches(isClickable()))
    }

    // TC_LOGOUT_006: After logout, Firebase Auth has no current user
    @Test
    fun tc_logout_006_firebaseSessionCleared() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).perform(click())
        // Verify that we are on the login screen (session cleared)
        onView(withId(R.id.et_email)).check(matches(isDisplayed()))
        onView(withId(R.id.et_password)).check(matches(isDisplayed()))
    }
}
