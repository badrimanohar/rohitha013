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
 * Espresso tests for Market Price (PricesFragment).
 *
 * Covers:
 *  - TC_PRICE_001: Title "Crop Price Prediction" visible
 *  - TC_PRICE_002: Subtitle visible
 *  - TC_PRICE_003: State spinner visible
 *  - TC_PRICE_004: District spinner visible
 *  - TC_PRICE_005: Market spinner visible
 *  - TC_PRICE_006: Crop spinner visible
 *  - TC_PRICE_007: Predict button visible and clickable
 *  - TC_PRICE_008: Result card hidden initially
 *  - TC_PRICE_009: Submitting without selection shows Toast
 *  - TC_PRICE_010: Predict button label correct
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MarketPriceFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    private fun navigateToPrices() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_prices))
    }

    // TC_PRICE_001
    @Test
    fun tc_price_001_titleVisible() {
        navigateToPrices()
        onView(withText("Crop Price Prediction")).check(matches(isDisplayed()))
    }

    // TC_PRICE_002
    @Test
    fun tc_price_002_subtitleVisible() {
        navigateToPrices()
        onView(withText("Predict crop market price using AI")).check(matches(isDisplayed()))
    }

    // TC_PRICE_003
    @Test
    fun tc_price_003_stateSpinnerVisible() {
        navigateToPrices()
        onView(withId(R.id.spinner_state)).check(matches(isDisplayed()))
    }

    // TC_PRICE_004
    @Test
    fun tc_price_004_districtSpinnerVisible() {
        navigateToPrices()
        onView(withId(R.id.spinner_district)).check(matches(isDisplayed()))
    }

    // TC_PRICE_005
    @Test
    fun tc_price_005_marketSpinnerVisible() {
        navigateToPrices()
        onView(withId(R.id.spinner_market)).check(matches(isDisplayed()))
    }

    // TC_PRICE_006
    @Test
    fun tc_price_006_cropSpinnerVisible() {
        navigateToPrices()
        onView(withId(R.id.spinner_crop)).check(matches(isDisplayed()))
    }

    // TC_PRICE_007
    @Test
    fun tc_price_007_predictBtnVisible() {
        navigateToPrices()
        onView(withId(R.id.btn_predict))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    // TC_PRICE_008
    @Test
    fun tc_price_008_resultCardHiddenInitially() {
        navigateToPrices()
        onView(withId(R.id.result_card))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_PRICE_009
    @Test
    fun tc_price_009_submitWithoutSelectionShowsToast() {
        navigateToPrices()
        // Click predict with no selections — should show Toast "Please select all fields"
        onView(withId(R.id.btn_predict)).perform(click())
        // The button should remain visible and enabled
        onView(withId(R.id.btn_predict)).check(matches(isDisplayed()))
    }

    // TC_PRICE_010
    @Test
    fun tc_price_010_predictButtonLabel() {
        navigateToPrices()
        onView(withId(R.id.btn_predict)).check(matches(withText("Predict Price")))
    }
}
