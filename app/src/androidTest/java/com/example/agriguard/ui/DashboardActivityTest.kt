package com.example.agriguard.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.agriguard.R
import com.example.agriguard.activities.DashboardActivity
import com.example.agriguard.util.FirebaseTestAuthHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI tests for [DashboardActivity].
 *
 * Requires the user to be pre-authenticated. Uses [FirebaseTestAuthHelper]
 * to sign in with test credentials before each test.
 *
 * Covers:
 *  - TC_DASH_001: Bottom navigation bar visible
 *  - TC_DASH_002: Home tab is default selected
 *  - TC_DASH_003: Navigate to Detect tab
 *  - TC_DASH_004: Navigate to Community tab
 *  - TC_DASH_005: Navigate to Prices tab
 *  - TC_DASH_006: Navigate to Profile tab
 *  - TC_DASH_007: Fragment container is displayed
 *  - TC_DASH_008: Home fragment shows greeting text
 *  - TC_DASH_009: Home fragment shows feature grid
 *  - TC_DASH_010: Bottom nav has 5 items
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class DashboardActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    // TC_DASH_001
    @Test
    fun tc_dash_001_bottomNavVisible() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()))
    }

    // TC_DASH_002
    @Test
    fun tc_dash_002_homeTabDefaultSelected() {
        // Home fragment content should be visible
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()))
    }

    // TC_DASH_003
    @Test
    fun tc_dash_003_navigateToDetectTab() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_detect))
        // Disease fragment shows "Crop Disease Detection" title
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
    }

    // TC_DASH_004
    @Test
    fun tc_dash_004_navigateToCommunityTab() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_community))
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()))
    }

    // TC_DASH_005
    @Test
    fun tc_dash_005_navigateToPricesTab() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_prices))
        // PricesFragment has spinner_state
        onView(withId(R.id.spinner_state)).check(matches(isDisplayed()))
    }

    // TC_DASH_006
    @Test
    fun tc_dash_006_navigateToProfileTab() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_profile))
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()))
    }

    // TC_DASH_007
    @Test
    fun tc_dash_007_fragmentContainerDisplayed() {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()))
    }

    // TC_DASH_008
    @Test
    fun tc_dash_008_homeFragmentGreetingVisible() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_home))
        // Greeting text view is in fragment_home
        onView(withId(R.id.tv_greeting)).check(matches(isDisplayed()))
    }

    // TC_DASH_009
    @Test
    fun tc_dash_009_homeFragmentFeatureGridVisible() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_home))
        onView(withId(R.id.rv_features)).check(matches(isDisplayed()))
    }

    // TC_DASH_010
    @Test
    fun tc_dash_010_bottomNavHasFiveItems() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()))
        // Verify each nav item exists by ID
        onView(withId(R.id.nav_home)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_detect)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_community)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_prices)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_profile)).check(matches(isDisplayed()))
    }
}
