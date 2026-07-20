package com.example.agriguard.ui

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.agriguard.R
import com.example.agriguard.activities.DashboardActivity
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso tests for Disease Detection fragment (nav_detect).
 *
 * Covers:
 *  - TC_DISEASE_001: Title "Crop Disease Detection" visible
 *  - TC_DISEASE_002: Upload card placeholder visible initially
 *  - TC_DISEASE_003: Capture button visible and clickable
 *  - TC_DISEASE_004: Gallery button visible and clickable
 *  - TC_DISEASE_005: Detect button hidden before image selected
 *  - TC_DISEASE_006: Remove image button hidden initially
 *  - TC_DISEASE_007: Result card hidden before detection
 *  - TC_DISEASE_008: Error card hidden initially
 *  - TC_DISEASE_009: Progress bar hidden initially
 *  - TC_DISEASE_010: Subtitle text visible
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class DiseaseDetectionFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    @get:Rule
    val cameraPermission: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.CAMERA)

    private fun navigateToDetect() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_detect))
    }

    // TC_DISEASE_001
    @Test
    fun tc_disease_001_titleVisible() {
        navigateToDetect()
        onView(withId(R.id.tv_title))
            .check(matches(withText("Crop Disease Detection")))
    }

    // TC_DISEASE_002
    @Test
    fun tc_disease_002_uploadPlaceholderVisible() {
        navigateToDetect()
        onView(withId(R.id.upload_placeholder)).check(matches(isDisplayed()))
    }

    // TC_DISEASE_003
    @Test
    fun tc_disease_003_captureBtnVisible() {
        navigateToDetect()
        onView(withId(R.id.btn_capture)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_capture)).check(matches(isClickable()))
    }

    // TC_DISEASE_004
    @Test
    fun tc_disease_004_galleryBtnVisible() {
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_gallery)).check(matches(isClickable()))
    }

    // TC_DISEASE_005
    @Test
    fun tc_disease_005_detectBtnHiddenBeforeImage() {
        navigateToDetect()
        onView(withId(R.id.btn_detect))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_DISEASE_006
    @Test
    fun tc_disease_006_removeImageBtnHiddenInitially() {
        navigateToDetect()
        onView(withId(R.id.btn_remove_image))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_DISEASE_007
    @Test
    fun tc_disease_007_resultCardHiddenInitially() {
        navigateToDetect()
        onView(withId(R.id.result_card))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_DISEASE_008
    @Test
    fun tc_disease_008_errorCardHiddenInitially() {
        navigateToDetect()
        onView(withId(R.id.error_card))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_DISEASE_009
    @Test
    fun tc_disease_009_progressBarHiddenInitially() {
        navigateToDetect()
        onView(withId(R.id.progress_bar))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_DISEASE_010
    @Test
    fun tc_disease_010_subtitleVisible() {
        navigateToDetect()
        onView(withId(R.id.tv_subtitle))
            .check(matches(withText("Upload crop image to detect disease")))
    }
}
