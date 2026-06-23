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
import com.example.agriguard.api.PlantResponse
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
 * Fixed to handle complex JSON structures and provide robust error handling without crashes.
 */
class DetectionRepository(private val context: Context? = null) {
    private val TAG = "API_DEBUG"
    
    private val mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val mStorage = FirebaseStorage.getInstance("gs://agriguard-29853.firebasestorage.app")
    private val userId = FirebaseAuth.getInstance().uid

    /**
     * Calls Kindwise API for analysis with safe parsing and complete response logging.
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

            // Requirement 1, 2 & 10: Complete Response Logging
            val responseBody = response.body()
            val fullJsonResponse = Gson().toJson(responseBody)
            
            Log.d("CROP_RESPONSE", "Request URL: ${response.raw().request.url}")
            Log.d("CROP_RESPONSE", "Response Code: ${response.code()}")
            Log.d("CROP_RESPONSE", "Response Message: ${response.message()}")
            Log.d("CROP_RESPONSE", "Full JSON Response: $fullJsonResponse")
            
            Log.d("API_DEBUG", "Response Code: ${response.code()}")
            Log.d("API_DEBUG", "Response Body: $fullJsonResponse")

            if (response.isSuccessful) {
                // Requirement 11 & 12: Dynamic checks for null values
                val result = responseBody?.result
                if (result == null) {
                    return PredictionResult(status = "Error", symptoms = "Unable to process disease detection results")
                }

                // Plant Validation (Threshold at 60% for better UX)
                val isPlantProb = result.isPlant?.probability ?: 0.0
                if (isPlantProb < 0.6) {
                    return PredictionResult(
                        status = "Invalid",
                        cropName = "Unknown",
                        description = "Uploaded image is not a plant. Please upload a clear crop leaf image."
                    )
                }

                val prediction = PredictionResult()
                
                // Extract Crop Name
                val plantSuggestion = result.classification?.suggestions?.firstOrNull()
                val cropName = plantSuggestion?.name?.replaceFirstChar { it.uppercase() } ?: "Unknown Crop"
                prediction.cropName = cropName

                if (cropName == "Unknown Crop") {
                    prediction.status = "Invalid Crop Image"
                    prediction.description = "Please upload a clearer image of the crop."
                    return prediction
                }

                // Requirement 11 & 12: Dynamically check health assessment and disease containers
                val healthAssessment = result.healthAssessment
                val diseaseContainer = result.disease
                
                when {
                    healthAssessment != null -> {
                        val isHealthy = healthAssessment.isHealthy ?: false
                        val topDisease = healthAssessment.diseases?.firstOrNull()
                        
                        if (isHealthy) {
                            setHealthyStatus(prediction, healthAssessment.isHealthyProbability)
                        } else if (topDisease != null) {
                            mapDiseaseToPrediction(prediction, topDisease)
                        } else {
                            // Requirement 6 & 12: Meaningful fallback instead of error message
                            setHealthyStatus(prediction, 1.0)
                        }
                    }
                    diseaseContainer != null -> {
                        val topDisease = diseaseContainer.suggestions?.firstOrNull()
                        if (topDisease != null && topDisease.name?.contains("healthy", ignoreCase = true) != true) {
                            mapDiseaseToPrediction(prediction, topDisease)
                        } else {
                            setHealthyStatus(prediction, topDisease?.probability)
                        }
                    }
                    else -> {
                        // Requirement 8: Default to healthy if no specific disease data is found
                        setHealthyStatus(prediction, 1.0)
                    }
                }
                
                return prediction
            } else {
                return PredictionResult(status = "Error", symptoms = "API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call or parsing", e)
            return PredictionResult(status = "Error", symptoms = "Unable to process disease detection results")
        }
    }

    private fun setHealthyStatus(prediction: PredictionResult, confidence: Double?) {
        prediction.status = "Healthy"
        prediction.diseaseName = "None"
        prediction.confidence = String.format(Locale.US, "%.1f%%", (confidence ?: 1.0) * 100)
        prediction.description = "The plant appears to be in good health with no visible diseases."
    }

    private fun mapDiseaseToPrediction(prediction: PredictionResult, disease: Suggestion) {
        // Requirement 7 & 11: Extract all details safely with null checks
        prediction.status = "Diseased"
        prediction.diseaseName = disease.name?.replaceFirstChar { it.uppercase() } ?: "Unknown Disease"
        prediction.confidence = String.format(Locale.US, "%.1f%%", (disease.probability ?: 0.0) * 100)
        
        val details = disease.details
        if (details == null) {
            prediction.description = "No specific data available for this disease."
            return
        }

        prediction.description = details.description ?: "No description available."
        prediction.causes = details.cause ?: "Biological or environmental factors."
        
        // Handle Symptoms (Object-safe extraction)
        prediction.symptoms = details.symptoms?.text ?: details.symptoms?.description ?: "Visible damage or spotting on the leaves."
        
        val treatment = details.treatment
        if (treatment != null) {
            // Mapping Biological to Fertilizer and Chemical to Pesticide as requested
            prediction.fertilizer = treatment.biological?.joinToString(", ") ?: "Maintain balanced soil nutrition."
            prediction.pesticides = treatment.chemical?.joinToString(", ") ?: "None recommended or apply broad-spectrum fungicides."
            prediction.preventionTips = treatment.prevention?.joinToString("\n") ?: "Ensure proper spacing and irrigation."
        } else {
            prediction.fertilizer = "Consult agricultural experts for organic treatments."
            prediction.pesticides = "Use appropriate fungicides if the infection spreads."
            prediction.preventionTips = "Practice crop rotation and field sanitation."
        }
        prediction.irrigationTips = "Avoid overhead irrigation to reduce moisture on leaves."
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
            Log.e(TAG, "Critical failure: ${e.message}")
        }
    }
}
