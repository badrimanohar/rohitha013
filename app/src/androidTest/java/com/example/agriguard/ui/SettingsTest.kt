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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso tests for Settings / Profile-level app settings.
 *
 * AgriGuard does not have a dedicated SettingsActivity. All configurable
 * user settings (profile update, account deletion, notification toggle)
 * are accessed from the Profile fragment. These tests document and validate
 * each settings-level action available to the user.
 *
 * Covers:
 *  - TC_SETTINGS_001: Profile tab acts as the Settings hub
 *  - TC_SETTINGS_002: Edit Profile entry-point is accessible
 *  - TC_SETTINGS_003: Full Name field is editable in settings
 *  - TC_SETTINGS_004: Phone Number field is editable
 *  - TC_SETTINGS_005: Location field is editable
 *  - TC_SETTINGS_006: Preferred Crops field is editable
 *  - TC_SETTINGS_007: Bio field is editable
 *  - TC_SETTINGS_008: Save updates the profile (Save btn is clickable)
 *  - TC_SETTINGS_009: Delete Account option is accessible
 *  - TC_SETTINGS_010: Bottom nav persists while in profile/settings
 *  - TC_SETTINGS_011: Toolbar shows "Profile" title
 *  - TC_SETTINGS_012: Edit FAB for profile image is visible in edit mode
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class SettingsTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    private fun navigateToProfile() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_profile))
    }

    private fun enterEditMode() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click())
    }

    // TC_SETTINGS_001
    @Test
    fun tc_settings_001_profileIsSettingsHub() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_logout)).check(matches(isDisplayed()))
    }

    // TC_SETTINGS_002
    @Test
    fun tc_settings_002_editProfileEntryPoint() {
        navigateToProfile()
        onView(withId(R.id.btn_edit_profile))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    // TC_SETTINGS_003
    @Test
    fun tc_settings_003_nameFieldEditableInSettings() {
        enterEditMode()
        onView(withId(R.id.et_name))
            .check(matches(isEnabled()))
            .check(matches(isFocusable()))
    }

    // TC_SETTINGS_004
    @Test
    fun tc_settings_004_phoneFieldEditableInSettings() {
        enterEditMode()
        onView(withId(R.id.et_phone))
            .check(matches(isEnabled()))
            .check(matches(isFocusable()))
    }

    // TC_SETTINGS_005
    @Test
    fun tc_settings_005_locationFieldEditableInSettings() {
        enterEditMode()
        onView(withId(R.id.et_location))
            .check(matches(isEnabled()))
            .check(matches(isFocusable()))
    }

    // TC_SETTINGS_006
    @Test
    fun tc_settings_006_cropsFieldEditableInSettings() {
        enterEditMode()
        onView(withId(R.id.et_crops))
            .check(matches(isEnabled()))
            .check(matches(isFocusable()))
    }

    // TC_SETTINGS_007
    @Test
    fun tc_settings_007_bioFieldEditableInSettings() {
        enterEditMode()
        onView(withId(R.id.et_bio))
            .check(matches(isEnabled()))
            .check(matches(isFocusable()))
    }

    // TC_SETTINGS_008
    @Test
    fun tc_settings_008_saveBtnClickable() {
        enterEditMode()
        onView(withId(R.id.btn_save))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    // TC_SETTINGS_009
    @Test
    fun tc_settings_009_deleteAccountVisible() {
        navigateToProfile()
        onView(withId(R.id.btn_delete_account)).check(matches(isDisplayed()))
    }

    // TC_SETTINGS_010
    @Test
    fun tc_settings_010_bottomNavPersistsOnProfile() {
        navigateToProfile()
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()))
    }

    // TC_SETTINGS_011
    @Test
    fun tc_settings_011_toolbarShowsProfileTitle() {
        navigateToProfile()
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withText("Profile")).check(matches(isDisplayed()))
    }

    // TC_SETTINGS_012
    @Test
    fun tc_settings_012_fabEditImageVisibleInEditMode() {
        enterEditMode()
        onView(withId(R.id.fab_edit_image)).check(matches(isDisplayed()))
    }
}
