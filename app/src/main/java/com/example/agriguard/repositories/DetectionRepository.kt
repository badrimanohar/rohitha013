package com.example.agriguard.repositories

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import com.example.agriguard.BuildConfig
import com.example.agriguard.api.CropRequest
import com.example.agriguard.api.RetrofitClient
import com.example.agriguard.api.Suggestion
import com.example.agriguard.models.PredictionResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
 * Repository for handling Crop Disease Detection logic using Kindwise (Plant.id) API.
 * Optimized for Crop.Health integration with robust parsing and comprehensive logging.
 */
class DetectionRepository(private val context: Context? = null) {
    private val TAG = "API_DEBUG"
    
    private val mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val mStorage = FirebaseStorage.getInstance("gs://agriguard-29853.firebasestorage.app")
    private val userId = FirebaseAuth.getInstance().uid

    /**
     * Calls Crop.Health Service for analysis with mandatory logging and dynamic null checks.
     */
    suspend fun analyzeWithCropHealth(bitmap: Bitmap): PredictionResult {
        val apiKey = BuildConfig.PLANT_ID_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "YOUR_PLANT_ID_API_KEY_HERE") {
            return PredictionResult(status = "Error", symptoms = "Crop.Health API Key Missing")
        }

        if (!isNetworkAvailable()) {
            return PredictionResult(status = "Error", symptoms = "No Internet Connection.")
        }

        return try {
            val base64Image = encodeImageToBase64(bitmap)
            val request = CropRequest(images = listOf(base64Image))
            
            val response = RetrofitClient.instance.identifyCrop(
                apiKey = apiKey,
                request = request
            )

            // Requirement 1, 2 & 10: Complete response logging for debugging
            val responseBody = response.body()
            
            Log.d("CROP_RESPONSE", Gson().toJson(response.body())) // Requirement 1
            Log.d("CROP_RESPONSE", "Request URL: ${response.raw().request.url}") // Requirement 2
            Log.d("CROP_RESPONSE", "Response Code: ${response.code()}") // Requirement 2
            Log.d("CROP_RESPONSE", "Response Message: ${response.message()}") // Requirement 2
            Log.d("CROP_RESPONSE", "Full JSON Response: ${Gson().toJson(response.body())}") // Requirement 2
            
            Log.d("API_DEBUG", "Response Code: ${response.code()}") // Requirement 10
            Log.d("API_DEBUG", "Response Body: ${Gson().toJson(response.body())}") // Requirement 10

            if (response.isSuccessful) {
                // Requirement 11: Dynamic checks for null values
                if (responseBody == null) {
                    Log.e(TAG, "Response body is null")
                    return PredictionResult(status = "Error", symptoms = "Unable to process disease detection results")
                }

                val result = responseBody.result
                if (result == null) {
                    Log.e(TAG, "Result block is null")
                    return PredictionResult(status = "Error", symptoms = "Unable to process disease detection results")
                }

                // 1. Plant Validation - Verify if the uploaded image is a crop/plant
                val isPlantProb = result.isPlant?.probability ?: 0.0
                val hasDiseaseSuggestions = result.disease?.suggestions?.isNotEmpty() ?: false
                val hasHealthSuggestions = result.healthAssessment?.diseases?.isNotEmpty() ?: false

                // If it's not a plant and has no diagnostic data, mark as Invalid
                if (isPlantProb < 0.5 && !hasDiseaseSuggestions && !hasHealthSuggestions) {
                    return PredictionResult(
                        status = "Invalid",
                        cropName = "Unknown Crop",
                        description = "Uploaded image is not a plant. Please upload a clear crop leaf image.",
                        isCropIdentified = false
                    )
                }

                val prediction = PredictionResult()
                
                // 2. Extract Real Crop Name based on the leaf
                val plantSuggestion = result.classification?.suggestions?.firstOrNull()
                val apiCropName = plantSuggestion?.details?.commonNames?.firstOrNull()?.replaceFirstChar { it.uppercase() }
                    ?: plantSuggestion?.name?.replaceFirstChar { it.uppercase() }
                    ?: "Plant"

                // Always show the real crop name if it's a plant
                prediction.cropName = apiCropName
                prediction.isCropIdentified = apiCropName != "Unknown Crop"

                // 3. Process Disease and Health status with confidence threshold
                val healthAssessment = result.healthAssessment
                val isHealthyBlock = result.isHealthy
                val diseaseContainer = result.disease
                
                var finalConfidence = 0.0
                
                if (healthAssessment != null) {
                    val isHealthy = healthAssessment.isHealthy ?: ((healthAssessment.isHealthyProbability ?: 0.0) > 0.5)
                    val topDisease = healthAssessment.diseases?.firstOrNull()
                    finalConfidence = if (isHealthy) (healthAssessment.isHealthyProbability ?: 1.0) else (topDisease?.probability ?: 0.0)
                    
                    if (finalConfidence < 0.5) {
                        return PredictionResult(
                            status = "Invalid",
                            cropName = "Unknown Crop",
                            description = "Low detection confidence (${String.format(Locale.US, "%.1f%%", finalConfidence * 100)}). Please upload a clearer crop leaf image.",
                            isCropIdentified = false
                        )
                    }

                    if (isHealthy) {
                        setHealthyResult(prediction, healthAssessment.isHealthyProbability)
                    } else if (topDisease != null) {
                        mapDiseaseToPrediction(prediction, topDisease)
                    } else {
                        setFallbackHealthy(prediction)
                    }
                } 
                else if (isHealthyBlock != null || diseaseContainer != null) {
                    val isHealthy = isHealthyBlock?.binary ?: ((isHealthyBlock?.probability ?: 0.0) > 0.5)
                    val topDisease = diseaseContainer?.suggestions?.firstOrNull()
                    finalConfidence = if (isHealthy) (isHealthyBlock?.probability ?: 1.0) else (topDisease?.probability ?: 0.0)

                    if (finalConfidence < 0.5) {
                        return PredictionResult(
                            status = "Invalid",
                            cropName = "Unknown Crop",
                            description = "Low detection confidence (${String.format(Locale.US, "%.1f%%", finalConfidence * 100)}). Please upload a clearer crop leaf image.",
                            isCropIdentified = false
                        )
                    }

                    if (isHealthy && (topDisease == null || (topDisease.probability ?: 0.0) < 0.5)) {
                        setHealthyResult(prediction, isHealthyBlock?.probability)
                    } else if (topDisease != null) {
                        mapDiseaseToPrediction(prediction, topDisease)
                    } else {
                        setFallbackHealthy(prediction)
                    }
                }
                else {
                    setFallbackHealthy(prediction)
                }
                
                return prediction
            } else {
                Log.e(TAG, "API Error: ${response.code()}")
                return PredictionResult(status = "Error", symptoms = "API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during analysis: ${e.message}", e)
            return PredictionResult(status = "Error", symptoms = "Unable to process disease detection results")
        }
    }

    private fun setHealthyResult(prediction: PredictionResult, probability: Double?) {
        prediction.status = "Healthy"
        prediction.diseaseName = "None"
        prediction.confidence = String.format(Locale.US, "%.1f%%", (probability ?: 1.0) * 100)
        prediction.description = "Visual analysis indicates this plant is healthy and showing no signs of disease."
        prediction.preventionTips = "Continue regular monitoring and proper irrigation."
    }

    private fun setFallbackHealthy(prediction: PredictionResult) {
        // Requirement 6: Meaningful fallback instead of error
        prediction.status = "Healthy"
        prediction.diseaseName = "None"
        prediction.confidence = "N/A"
        prediction.description = "The plant appears to be in good health based on visual inspection."
    }

    private fun mapDiseaseToPrediction(prediction: PredictionResult, disease: Suggestion) {
        // Requirement 5 & 7: Extract all details safely to prevent NPE
        prediction.status = "Diseased"
        prediction.diseaseName = disease.name?.replaceFirstChar { it.uppercase() } ?: "Unknown Disease"
        prediction.confidence = String.format(Locale.US, "%.1f%%", (disease.probability ?: 0.0) * 100)
        
        val details = disease.details
        prediction.description = details?.description ?: "No description available for this condition."
        prediction.causes = details?.cause ?: "Environmental factors or pathogen infection."
        
        // Handle Symptoms field which can be nested
        prediction.symptoms = details?.symptoms?.text ?: details?.symptoms?.description ?: "Visual signs of plant distress observed."
        
        // Requirement 7: Extract treatment, fertilizer, pesticide, and prevention
        val treatment = details?.treatment
        prediction.fertilizer = treatment?.biological?.joinToString(", ") ?: "Nutrient balancing recommended."
        prediction.pesticides = treatment?.chemical?.joinToString(", ") ?: "Targeted treatment if condition persists."
        prediction.preventionTips = treatment?.prevention?.joinToString("\n") ?: "Monitor environmental conditions."
        prediction.irrigationTips = "Adjust watering schedule according to plant needs."
    }

    private fun isNetworkAvailable(): Boolean {
        if (context == null) return true 
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun saveDetectionHistory(prediction: PredictionResult, bitmap: Bitmap) {
        val currentUserId = userId ?: return
        try {
            val historyId = mDatabase.child("users").child(currentUserId).child("history").push().key ?: return
            prediction.id = historyId
            val imagePath = "detection_images/$currentUserId/$historyId.jpg"
            val imageRef = mStorage.reference.child(imagePath)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
            val data = baos.toByteArray()
            try {
                imageRef.putBytes(data).await()
                val uri = imageRef.downloadUrl.await()
                prediction.imageUrl = uri.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Upload failed: ${e.message}")
            }
            mDatabase.child("users").child(currentUserId).child("history").child(historyId)
                .setValue(prediction)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "History Save failure: ${e.message}")
        }
    }
}
