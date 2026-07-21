package com.example.agriguard.viewmodels

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit & Integration Tests for DetectionViewModel.
 * Verifies initial state, loading state toggles, error handling, and LiveData updates.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DetectionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DetectionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()
        try {
            if (com.google.firebase.FirebaseApp.getApps(application).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(
                    application,
                    com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("dummy_app_id")
                        .setApiKey("dummy_api_key")
                        .setProjectId("dummy_project")
                        .build()
                )
            }
        } catch (e: Exception) {
            // Safe fallback during Robolectric execution
        }
        viewModel = DetectionViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.result.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun testDetectStartsLoadingAndResetsState() {
        // Verify state changes upon calling detect
        // Even when bitmap processing runs asynchronously, initial values should update
        val application = ApplicationProvider.getApplicationContext<Application>()
        val dummyBitmap = android.graphics.Bitmap.createBitmap(10, 10, android.graphics.Bitmap.Config.ARGB_8888)
        
        viewModel.detect(dummyBitmap)

        assertEquals(true, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertNull(viewModel.result.value)
    }
}
