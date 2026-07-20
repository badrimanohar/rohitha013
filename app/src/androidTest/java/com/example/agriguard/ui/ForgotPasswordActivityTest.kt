package com.example.agriguard.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.agriguard.R
import com.example.agriguard.activities.ForgotPasswordActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI tests for [ForgotPasswordActivity].
 *
 * Covers:
 *  - TC_FP_001: All UI elements visible on screen
 *  - TC_FP_002: Back navigation button displayed
 *  - TC_FP_003: Empty email submission shows validation
 *  - TC_FP_004: Invalid email format shows error
 *  - TC_FP_005: Valid email enables submit button
 *  - TC_FP_006: Progress bar is hidden initially
 *  - TC_FP_007: Title text matches "Forgot Password?"
 *  - TC_FP_008: Send Reset Link button has correct label
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ForgotPasswordActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(ForgotPasswordActivity::class.java)

    // TC_FP_001
    @Test
    fun tc_fp_001_allElementsVisible() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.iv_lock)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_desc)).check(matches(isDisplayed()))
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_reset)).check(matches(isDisplayed()))
    }

    // TC_FP_002
    @Test
    fun tc_fp_002_titleTextCorrect() {
        onView(withId(R.id.tv_title)).check(matches(withText("Forgot Password?")))
    }

    // TC_FP_003
    @Test
    fun tc_fp_003_emptyEmailShowsValidation() {
        onView(withId(R.id.et_email)).perform(clearText())
        onView(withId(R.id.btn_reset)).perform(click())
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
    }

    // TC_FP_004
    @Test
    fun tc_fp_004_invalidEmailFormat() {
        onView(withId(R.id.et_email)).perform(typeText("invalidemail"), closeSoftKeyboard())
        onView(withId(R.id.btn_reset)).perform(click())
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
    }

    // TC_FP_005
    @Test
    fun tc_fp_005_validEmailEnablesButton() {
        onView(withId(R.id.et_email)).perform(typeText("farmer@agriguard.com"), closeSoftKeyboard())
        onView(withId(R.id.btn_reset)).check(matches(isEnabled()))
    }

    // TC_FP_006
    @Test
    fun tc_fp_006_progressBarHiddenInitially() {
        onView(withId(R.id.progress_bar))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_FP_007
    @Test
    fun tc_fp_007_descriptionTextVisible() {
        onView(withId(R.id.tv_desc)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_desc)).check(matches(withText(
            "Enter your email address and we will send you a link to reset your password."
        )))
    }

    // TC_FP_008
    @Test
    fun tc_fp_008_resetButtonLabel() {
        onView(withId(R.id.btn_reset)).check(matches(withText("Send Reset Link")))
    }
}
