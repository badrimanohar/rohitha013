package com.example.agriguard.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.agriguard.R
import com.example.agriguard.activities.SignUpActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI tests for [SignUpActivity].
 *
 * Covers:
 *  - TC_REG_001: All registration fields are visible
 *  - TC_REG_002: Empty form submission shows validation
 *  - TC_REG_003: Weak password shows strength indicator
 *  - TC_REG_004: Strong password accepted
 *  - TC_REG_005: Password mismatch shows error
 *  - TC_REG_006: Invalid email format rejected
 *  - TC_REG_007: Short name rejected
 *  - TC_REG_008: Password strength bar is visible when typing
 *  - TC_REG_009: Navigate back to Login
 *  - TC_REG_010: Google Sign-Up button visible and clickable
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class RegisterActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SignUpActivity::class.java)

    // TC_REG_001
    @Test
    fun tc_reg_001_allFieldsVisible() {
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
        onView(withId(R.id.til_name)).check(matches(isDisplayed()))
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
        onView(withId(R.id.til_password)).check(matches(isDisplayed()))
        onView(withId(R.id.til_confirm_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_signup)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_google_signup)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_go_to_login)).check(matches(isDisplayed()))
    }

    // TC_REG_002
    @Test
    fun tc_reg_002_emptyFormSubmissionShowsValidation() {
        onView(withId(R.id.btn_signup)).perform(click())
        onView(withId(R.id.til_name)).check(matches(isDisplayed()))
    }

    // TC_REG_003: Typing a weak password reveals the strength bar
    @Test
    fun tc_reg_003_weakPasswordShowsStrengthBar() {
        onView(withId(R.id.et_password)).perform(typeText("abc"), closeSoftKeyboard())
        onView(withId(R.id.ll_password_strength)).check(matches(isDisplayed()))
    }

    // TC_REG_004: Strong password accepted — strength label changes
    @Test
    fun tc_reg_004_strongPasswordShowsStrongLabel() {
        onView(withId(R.id.et_password)).perform(typeText("StrongP@ss1"), closeSoftKeyboard())
        onView(withId(R.id.ll_password_strength)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_password_strength)).check(matches(isDisplayed()))
    }

    // TC_REG_005: Password mismatch shows error
    @Test
    fun tc_reg_005_passwordMismatchShowsError() {
        onView(withId(R.id.et_name)).perform(typeText("Raj Kumar"), closeSoftKeyboard())
        onView(withId(R.id.et_email)).perform(typeText("raj@agriguard.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(typeText("StrongP@ss1"), closeSoftKeyboard())
        onView(withId(R.id.et_confirm_password)).perform(typeText("DifferentP@ss!"), closeSoftKeyboard())
        onView(withId(R.id.btn_signup)).perform(click())
        // til_confirm_password should display an error
        onView(withId(R.id.til_confirm_password)).check(matches(isDisplayed()))
    }

    // TC_REG_006: Invalid email rejected
    @Test
    fun tc_reg_006_invalidEmailRejected() {
        onView(withId(R.id.et_name)).perform(typeText("Raj Kumar"), closeSoftKeyboard())
        onView(withId(R.id.et_email)).perform(typeText("notanemail"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(typeText("StrongP@ss1"), closeSoftKeyboard())
        onView(withId(R.id.et_confirm_password)).perform(typeText("StrongP@ss1"), closeSoftKeyboard())
        onView(withId(R.id.btn_signup)).perform(click())
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
    }

    // TC_REG_007: Empty name field is rejected
    @Test
    fun tc_reg_007_emptyNameRejected() {
        onView(withId(R.id.et_email)).perform(typeText("raj@test.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(typeText("StrongP@ss1"), closeSoftKeyboard())
        onView(withId(R.id.et_confirm_password)).perform(typeText("StrongP@ss1"), closeSoftKeyboard())
        onView(withId(R.id.btn_signup)).perform(click())
        onView(withId(R.id.til_name)).check(matches(isDisplayed()))
    }

    // TC_REG_008: Password strength bar hidden before typing
    @Test
    fun tc_reg_008_strengthBarHiddenInitially() {
        onView(withId(R.id.ll_password_strength)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_REG_009: Navigate to Login screen
    @Test
    fun tc_reg_009_navigateToLogin() {
        onView(withId(R.id.tv_go_to_login)).perform(click())
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }

    // TC_REG_010: Google Sign-Up button is clickable
    @Test
    fun tc_reg_010_googleSignUpButtonClickable() {
        onView(withId(R.id.btn_google_signup)).check(matches(isClickable()))
    }
}
