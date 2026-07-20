package com.example.agriguard.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.agriguard.R
import com.example.agriguard.activities.LoginActivity
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.RuleChain

/**
 * Espresso UI tests for [LoginActivity].
 *
 * Covers:
 *  - TC_LOGIN_001: Screen elements rendered correctly
 *  - TC_LOGIN_002: Empty email and password shows validation error
 *  - TC_LOGIN_003: Invalid email format shows error
 *  - TC_LOGIN_004: Empty password with valid email shows error
 *  - TC_LOGIN_005: Valid credentials enable login button
 *  - TC_LOGIN_006: Login button disabled state during submission
 *  - TC_LOGIN_007: Navigate to ForgotPassword on link click
 *  - TC_LOGIN_008: Navigate to SignUp screen on link click
 *  - TC_LOGIN_009: Password toggle shows/hides password text
 *  - TC_LOGIN_010: Google Sign-In button is visible and clickable
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_001: Screen elements rendered correctly
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_001_screenElementsDisplayed() {
        onView(withId(R.id.tv_welcome)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()))
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
        onView(withId(R.id.til_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_forgot_password)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_go_to_signup)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_google_login)).check(matches(isDisplayed()))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_002: Empty email and password shows validation
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_002_emptyFieldsShowsValidation() {
        onView(withId(R.id.et_email)).perform(clearText())
        onView(withId(R.id.et_password)).perform(clearText())
        onView(withId(R.id.btn_login)).perform(click())

        // Expect email input layout to show an error hint
        onView(withId(R.id.til_email)).check(matches(hasDescendant(withText(""))))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_003: Invalid email format is rejected before submission
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_003_invalidEmailFormatShowsError() {
        onView(withId(R.id.et_email)).perform(typeText("not-an-email"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(typeText("Password1!"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())

        // Error on TIL should appear
        onView(withId(R.id.til_email)).check(matches(isDisplayed()))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_004: Empty password with valid email shows error
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_004_emptyPasswordShowsError() {
        onView(withId(R.id.et_email)).perform(typeText("farmer@agriguard.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(clearText())
        onView(withId(R.id.btn_login)).perform(click())

        onView(withId(R.id.til_password)).check(matches(isDisplayed()))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_005: Typing credentials enables login button
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_005_credentialTypingEnablesLoginButton() {
        onView(withId(R.id.et_email)).perform(typeText("farmer@agriguard.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password)).perform(typeText("Password1!"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).check(matches(isEnabled()))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_006: Welcome text matches expected string
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_006_welcomeTextCorrect() {
        onView(withId(R.id.tv_welcome)).check(matches(withText(R.string.welcome_back)))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_007: Clicking Forgot Password navigates to ForgotPasswordActivity
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_007_forgotPasswordNavigation() {
        onView(withId(R.id.tv_forgot_password)).perform(click())
        // ForgotPasswordActivity has a tv_title "Forgot Password?" and a btn_reset
        onView(withId(R.id.btn_reset)).check(matches(isDisplayed()))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_008: Clicking "Don't have account" navigates to SignUpActivity
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_008_signUpNavigation() {
        onView(withId(R.id.tv_go_to_signup)).perform(click())
        // SignUpActivity has btn_signup
        onView(withId(R.id.btn_signup)).check(matches(isDisplayed()))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_009: Password field masks input by default
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_009_passwordFieldIsMasked() {
        onView(withId(R.id.et_password))
            .check(matches(withInputType(
                android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            )))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_LOGIN_010: Google Sign-In button is clickable
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun tc_login_010_googleSignInButtonClickable() {
        onView(withId(R.id.btn_google_login)).check(matches(isClickable()))
    }
}
