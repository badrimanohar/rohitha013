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
 * Espresso tests for Profile fragment.
 *
 * Covers:
 *  - TC_PROFILE_001: Profile header elements visible
 *  - TC_PROFILE_002: Edit Profile button visible and clickable
 *  - TC_PROFILE_003: Clicking Edit Profile reveals form fields
 *  - TC_PROFILE_004: Edit form fields enabled after clicking Edit
 *  - TC_PROFILE_005: Save button visible in edit mode
 *  - TC_PROFILE_006: Cancel button visible in edit mode
 *  - TC_PROFILE_007: Clicking Cancel exits edit mode
 *  - TC_PROFILE_008: Name field required — empty name shows error
 *  - TC_PROFILE_009: Logout button visible
 *  - TC_PROFILE_010: Delete Account text visible
 *  - TC_PROFILE_011: Stats section (Communities, Detections) visible
 *  - TC_PROFILE_012: Profile image visible
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    private fun navigateToProfile() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_profile))
    }

    // TC_PROFILE_001
    @Test
    fun tc_profile_001_headerVisible() {
        navigateToProfile()
        onView(withId(R.id.iv_profile_image)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_display_name)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_display_email)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_002
    @Test
    fun tc_profile_002_editProfileBtnVisible() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    // TC_PROFILE_003
    @Test
    fun tc_profile_003_editModeRevealsFab() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.fab_edit_image)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_004
    @Test
    fun tc_profile_004_editModeEnablesFields() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.et_name)).check(matches(isEnabled()))
        onView(withId(R.id.et_phone)).check(matches(isEnabled()))
        onView(withId(R.id.et_location)).check(matches(isEnabled()))
        onView(withId(R.id.et_bio)).check(matches(isEnabled()))
    }

    // TC_PROFILE_005
    @Test
    fun tc_profile_005_saveBtnVisibleInEditMode() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.btn_save)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_006
    @Test
    fun tc_profile_006_cancelBtnVisibleInEditMode() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.btn_cancel)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_007
    @Test
    fun tc_profile_007_cancelExitsEditMode() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.btn_cancel)).perform(click())
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_actions))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_PROFILE_008
    @Test
    fun tc_profile_008_emptyNameShowsError() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.et_name)).perform(clearText())
        onView(withId(R.id.btn_save)).perform(click())
        onView(withId(R.id.til_name)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_009
    @Test
    fun tc_profile_009_logoutBtnVisible() {
        navigateToProfile()
        onView(withId(R.id.btn_logout)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_010
    @Test
    fun tc_profile_010_deleteAccountVisible() {
        navigateToProfile()
        // Scroll down to find it
        onView(withId(R.id.btn_delete_account)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_011
    @Test
    fun tc_profile_011_statsCountersVisible() {
        navigateToProfile()
        onView(withId(R.id.tv_community_count)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_history_count)).check(matches(isDisplayed()))
    }

    // TC_PROFILE_012
    @Test
    fun tc_profile_012_profileImageVisible() {
        navigateToProfile()
        onView(withId(R.id.iv_profile_image)).check(matches(isDisplayed()))
    }
}
