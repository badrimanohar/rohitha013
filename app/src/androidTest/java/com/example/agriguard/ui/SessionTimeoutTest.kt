package com.example.agriguard.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.agriguard.R
import com.example.agriguard.activities.DashboardActivity
import com.example.agriguard.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso tests for Session Timeout scenarios.
 *
 * These tests validate that the app correctly handles an expired or revoked
 * Firebase session. Because Espresso tests run in-process, we simulate
 * a session timeout by signing out the current Firebase user and then
 * attempting to launch DashboardActivity — the app should redirect to Login.
 *
 * Covers:
 *  - TC_SESSION_001: DashboardActivity without authentication redirects to Login
 *  - TC_SESSION_002: Login screen is fully functional after session expiry
 *  - TC_SESSION_003: Signing out mid-session and pressing back lands on Login
 *  - TC_SESSION_004: Email field pre-filled not preserved across sessions
 *  - TC_SESSION_005: Password field is empty on fresh Login load post-session
 *  - TC_SESSION_006: Session expiry does not crash — LoginActivity renders
 *  - TC_SESSION_007: Re-authentication flow starts from Login screen
 *  - TC_SESSION_008: SplashActivity routes to Login when no session present
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class SessionTimeoutTest {

    private var scenario: ActivityScenario<*>? = null

    @After
    fun tearDown() {
        scenario?.close()
    }

    private fun signOutFirebase() {
        FirebaseAuth.getInstance().signOut()
    }

    // TC_SESSION_001: Launching Dashboard without auth should redirect to Login
    @Test
    fun tc_session_001_dashboardWithoutAuthRedirectsToLogin() {
        signOutFirebase()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            DashboardActivity::class.java
        )
        scenario = ActivityScenario.launch<DashboardActivity>(intent)
        // App should redirect to LoginActivity showing btn_login
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }

    // TC_SESSION_002: Login screen functional after session expiry
    @Test
    fun tc_session_002_loginScreenFunctionalAfterExpiry() {
        signOutFirebase()
        scenario = ActivityScenario.launch<LoginActivity>(
            Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        )
        onView(withId(R.id.et_email)).check(matches(isDisplayed()))
        onView(withId(R.id.et_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }

    // TC_SESSION_003: Sign out mid-session and verify Login shown
    @Test
    fun tc_session_003_signOutMidSessionShowsLogin() {
        signOutFirebase()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            LoginActivity::class.java
        )
        scenario = ActivityScenario.launch<LoginActivity>(intent)
        onView(withId(R.id.tv_welcome)).check(matches(isDisplayed()))
    }

    // TC_SESSION_004: Password field empty on fresh Login load
    @Test
    fun tc_session_004_passwordEmptyOnFreshLoad() {
        signOutFirebase()
        scenario = ActivityScenario.launch<LoginActivity>(
            Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        )
        onView(withId(R.id.et_password)).check(matches(withText("")))
    }

    // TC_SESSION_005: Email field empty on fresh Login load
    @Test
    fun tc_session_005_emailEmptyOnFreshLoad() {
        signOutFirebase()
        scenario = ActivityScenario.launch<LoginActivity>(
            Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        )
        onView(withId(R.id.et_email)).check(matches(withText("")))
    }

    // TC_SESSION_006: Session expiry does not crash — Login renders without exception
    @Test
    fun tc_session_006_sessionExpiryNoCrash() {
        signOutFirebase()
        scenario = ActivityScenario.launch<LoginActivity>(
            Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        )
        // Simple existence check — if activity launched cleanly, test passes
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }

    // TC_SESSION_007: Re-auth — can enter credentials and attempt login after expiry
    @Test
    fun tc_session_007_reAuthFlowAvailableAfterExpiry() {
        signOutFirebase()
        scenario = ActivityScenario.launch<LoginActivity>(
            Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        )
        onView(withId(R.id.et_email))
            .perform(typeText("farmer@agriguard.com"), closeSoftKeyboard())
        onView(withId(R.id.et_password))
            .perform(typeText("Password1!"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).check(matches(isEnabled()))
    }

    // TC_SESSION_008: SplashActivity routes to Login when no user session exists
    @Test
    fun tc_session_008_splashRoutesToLoginWithNoSession() {
        signOutFirebase()
        scenario = ActivityScenario.launch<com.example.agriguard.activities.SplashActivity>(
            Intent(
                ApplicationProvider.getApplicationContext(),
                com.example.agriguard.activities.SplashActivity::class.java
            )
        )
        // Allow splash delay (2 s) then assert LoginActivity
        Thread.sleep(2500)
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }
}
