package com.example.agriguard.ui

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.agriguard.R
import com.example.agriguard.activities.DashboardActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso intent tests for Image Upload, Camera and Gallery flows.
 *
 * Covers:
 *  - TC_IMG_001: Gallery button fires ACTION_GET_CONTENT intent
 *  - TC_IMG_002: Camera button fires ACTION_IMAGE_CAPTURE intent
 *  - TC_IMG_003: Selecting image from gallery shows preview
 *  - TC_IMG_004: Preview image view hidden before selection
 *  - TC_IMG_005: After gallery selection, detect button appears
 *  - TC_IMG_006: After camera capture, detect button appears
 *  - TC_IMG_007: Remove button shown after image selection
 *  - TC_IMG_008: Tapping remove resets to initial state
 *  - TC_IMG_009: Upload placeholder hidden after image selected
 *  - TC_IMG_010: Camera permission requested when capturing
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ImageUploadCameraGalleryTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    @get:Rule
    val cameraPermission: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun navigateToDetect() {
        onView(withId(R.id.bottom_navigation))
            .perform(NavigationViewActions.navigateTo(R.id.nav_detect))
    }

    /** Returns a fake 1×1 bitmap result for stubbing camera/gallery intents. */
    private fun stubBitmapResult(): Instrumentation.ActivityResult {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val data = Intent().apply { putExtra("data", bitmap) }
        return Instrumentation.ActivityResult(Activity.RESULT_OK, data)
    }

    /** Stubs the gallery picker with an OK result */
    private fun stubGalleryResult() {
        val result = stubBitmapResult()
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)
    }

    /** Stubs the camera with an OK result */
    private fun stubCameraResult() {
        val result = stubBitmapResult()
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)
    }

    // TC_IMG_001: Gallery button fires ACTION_GET_CONTENT
    @Test
    fun tc_img_001_galleryButtonFiresCorrectIntent() {
        stubGalleryResult()
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).perform(click())
        intended(hasAction(Intent.ACTION_GET_CONTENT))
    }

    // TC_IMG_002: Camera button fires ACTION_IMAGE_CAPTURE
    @Test
    fun tc_img_002_cameraButtonFiresCorrectIntent() {
        stubCameraResult()
        navigateToDetect()
        onView(withId(R.id.btn_capture)).perform(click())
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    // TC_IMG_003: After gallery selection, preview is displayed
    @Test
    fun tc_img_003_gallerySelectionShowsPreview() {
        stubGalleryResult()
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).perform(click())
        // After result returns, iv_preview should be visible
        onView(withId(R.id.iv_preview)).check(matches(isDisplayed()))
    }

    // TC_IMG_004: Preview hidden before image selected
    @Test
    fun tc_img_004_previewHiddenInitially() {
        navigateToDetect()
        onView(withId(R.id.iv_preview))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_IMG_005: Detect button visible after gallery selection
    @Test
    fun tc_img_005_detectBtnAppearsAfterGallery() {
        stubGalleryResult()
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).perform(click())
        onView(withId(R.id.btn_detect)).check(matches(isDisplayed()))
    }

    // TC_IMG_006: Detect button visible after camera capture
    @Test
    fun tc_img_006_detectBtnAppearsAfterCamera() {
        stubCameraResult()
        navigateToDetect()
        onView(withId(R.id.btn_capture)).perform(click())
        onView(withId(R.id.btn_detect)).check(matches(isDisplayed()))
    }

    // TC_IMG_007: Remove button visible after image selected
    @Test
    fun tc_img_007_removeButtonVisibleAfterSelection() {
        stubGalleryResult()
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).perform(click())
        onView(withId(R.id.btn_remove_image)).check(matches(isDisplayed()))
    }

    // TC_IMG_008: Tapping remove resets UI to initial state
    @Test
    fun tc_img_008_removeButtonResetsUI() {
        stubGalleryResult()
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).perform(click())
        onView(withId(R.id.btn_remove_image)).perform(click())
        onView(withId(R.id.upload_placeholder)).check(matches(isDisplayed()))
        onView(withId(R.id.iv_preview))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_IMG_009: Upload placeholder hidden after selection
    @Test
    fun tc_img_009_placeholderHiddenAfterSelection() {
        stubGalleryResult()
        navigateToDetect()
        onView(withId(R.id.btn_gallery)).perform(click())
        onView(withId(R.id.upload_placeholder))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // TC_IMG_010: Camera capture button is clickable with permission granted
    @Test
    fun tc_img_010_cameraBtnClickableWithPermission() {
        stubCameraResult()
        navigateToDetect()
        onView(withId(R.id.btn_capture)).check(matches(isClickable()))
    }
}
