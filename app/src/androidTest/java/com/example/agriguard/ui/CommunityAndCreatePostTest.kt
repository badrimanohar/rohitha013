package com.example.agriguard.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
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
 * Espresso tests for Community tab and [CommunityChatActivity] + [CreatePostActivity].
 *
 * Covers:
 *  - TC_COMM_001: Community fragment loads via nav tab
 *  - TC_COMM_002: Community RecyclerView is displayed
 *  - TC_COMM_003: Explore tab exists
 *  - TC_COMM_004: My Communities tab exists
 *  - TC_COMM_005: Community chat — message input visible
 *  - TC_COMM_006: Community chat — send button visible
 *  - TC_COMM_007: Community chat — chat list is displayed
 *  - TC_COMM_008: Send empty message does nothing
 *  - TC_COMM_009: Type and send a message
 *  - TC_COMM_010: Create Post screen elements visible
 *  - TC_COMM_011: Create Post — post button visible
 *  - TC_COMM_012: Create Post — posting triggers navigation back
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CommunityAndCreatePostTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    private fun navigateToCommunity() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_community))
    }

    // TC_COMM_001
    @Test
    fun tc_comm_001_communityFragmentLoads() {
        navigateToCommunity()
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()))
    }

    // TC_COMM_002
    @Test
    fun tc_comm_002_exploreRecyclerViewDisplayed() {
        navigateToCommunity()
        // Explore communities list
        onView(withId(R.id.rv_explore_communities)).check(matches(isDisplayed()))
    }

    // TC_COMM_003
    @Test
    fun tc_comm_003_joinedCommunitiesListDisplayed() {
        navigateToCommunity()
        onView(withId(R.id.rv_joined_communities)).check(matches(isDisplayed()))
    }

    // TC_COMM_004
    @Test
    fun tc_comm_004_communityTabLayoutDisplayed() {
        navigateToCommunity()
        onView(withId(R.id.tab_layout)).check(matches(isDisplayed()))
    }

    // TC_COMM_005: Chat message input visible in CommunityChatActivity
    @Test
    fun tc_comm_005_chatMessageInputVisible() {
        navigateToCommunity()
        // Tap the first community to open chat
        onView(withId(R.id.rv_explore_communities))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.et_message)).check(matches(isDisplayed()))
    }

    // TC_COMM_006: Chat send button visible
    @Test
    fun tc_comm_006_chatSendButtonVisible() {
        navigateToCommunity()
        onView(withId(R.id.rv_explore_communities))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.btn_send)).check(matches(isDisplayed()))
    }

    // TC_COMM_007: Chat RecyclerView displayed
    @Test
    fun tc_comm_007_chatRecyclerViewDisplayed() {
        navigateToCommunity()
        onView(withId(R.id.rv_explore_communities))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.rv_chat_messages)).check(matches(isDisplayed()))
    }

    // TC_COMM_008: Sending empty message does nothing
    @Test
    fun tc_comm_008_sendEmptyMessageDoesNothing() {
        navigateToCommunity()
        onView(withId(R.id.rv_explore_communities))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.et_message)).perform(clearText())
        onView(withId(R.id.btn_send)).perform(click())
        // Chat should still be open
        onView(withId(R.id.rv_chat_messages)).check(matches(isDisplayed()))
    }

    // TC_COMM_009: Type and send message
    @Test
    fun tc_comm_009_typeAndSendMessage() {
        navigateToCommunity()
        onView(withId(R.id.rv_explore_communities))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.et_message))
            .perform(typeText("Hello community!"), closeSoftKeyboard())
        onView(withId(R.id.btn_send)).perform(click())
        onView(withId(R.id.et_message)).check(matches(withText("")))
    }

    // TC_COMM_010: Create Post — toolbar visible
    @Test
    fun tc_comm_010_createPostToolbarVisible() {
        navigateToCommunity()
        // Tap FAB or create post button
        onView(withId(R.id.fab_create_post)).perform(click())
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    // TC_COMM_011: Create Post — btn_post visible
    @Test
    fun tc_comm_011_createPostBtnVisible() {
        navigateToCommunity()
        onView(withId(R.id.fab_create_post)).perform(click())
        onView(withId(R.id.btn_post)).check(matches(isDisplayed()))
    }

    // TC_COMM_012: Create Post — tapping post navigates back
    @Test
    fun tc_comm_012_postingNavigatesBack() {
        navigateToCommunity()
        onView(withId(R.id.fab_create_post)).perform(click())
        onView(withId(R.id.btn_post)).perform(click())
        // Should return to community screen
        onView(withId(R.id.rv_explore_communities)).check(matches(isDisplayed()))
    }
}
