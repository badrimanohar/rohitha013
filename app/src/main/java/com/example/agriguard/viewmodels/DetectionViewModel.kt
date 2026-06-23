package com.example.agriguard.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.agriguard.models.PredictionResult
import com.example.agriguard.repositories.DetectionRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Disease Detection.
 * Manages the state and UI updates for the detection process.
 */
class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "DetectionViewModel"
    private val repository = DetectionRepository(application)

    private val _result = MutableLiveData<PredictionResult?>()
    val result: LiveData<PredictionResult?> = _result

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Triggers the detection process using Kindwise API.
     */
    fun detect(bitmap: Bitmap) {
        _isLoading.value = true
        _error.value = null
        _result.value = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting detection pipeline...")
                
                val cropResult = repository.analyzeWithCropHealth(bitmap)

                when (cropResult.status) {
                    "Error" -> {
                        _error.postValue(cropResult.symptoms)
                    }
                    else -> {
                        // Pass both "Healthy", "Diseased", and "Invalid" to the activity
                        _result.postValue(cropResult)
                        
                        // Only save valid agricultural detections to history
                        if (cropResult.status == "Healthy" || cropResult.status == "Diseased") {
                            repository.saveDetectionHistory(cropResult, bitmap)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Detection Pipeline Error", e)
                _error.postValue("Detection Service Unavailable. Please try again.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
