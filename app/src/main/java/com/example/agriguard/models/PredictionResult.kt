package com.example.agriguard.models

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import java.util.Locale

/**
 * Model class for Crop Disease Detection results.
 * Optimized for Firebase Realtime Database with robust type handling for the confidence field.
 */
@IgnoreExtraProperties
class PredictionResult {
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String? = null

    @get:PropertyName("cropName")
    @set:PropertyName("cropName")
    var cropName: String = ""

    @get:PropertyName("diseaseName")
    @set:PropertyName("diseaseName")
    var diseaseName: String = ""

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = ""

    /**
     * Requirement 4 & 7: Single confidence property to handle multiple types from Firebase.
     * Using Any? allows Firebase to map String, Double, Float, Long, or Integer to this field.
     */
    @get:PropertyName("confidence")
    @set:PropertyName("confidence")
    var confidence: Any? = null

    @get:PropertyName("symptoms")
    @set:PropertyName("symptoms")
    var symptoms: String = ""

    @get:PropertyName("causes")
    @set:PropertyName("causes")
    var causes: String = ""

    @get:PropertyName("fertilizer")
    @set:PropertyName("fertilizer")
    var fertilizer: String = ""

    @get:PropertyName("pesticides")
    @set:PropertyName("pesticides")
    var pesticides: String = ""

    @get:PropertyName("preventionTips")
    @set:PropertyName("preventionTips")
    var preventionTips: String = ""

    @get:PropertyName("irrigationTips")
    @set:PropertyName("irrigationTips")
    var irrigationTips: String = ""

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = ""

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String? = null

    @get:PropertyName("cropIdentified")
    @set:PropertyName("cropIdentified")
    var isCropIdentified: Boolean = true

    // No-argument constructor for Firebase (Requirement 5)
    constructor()

    constructor(
        id: String? = null,
        cropName: String = "",
        diseaseName: String = "",
        status: String = "",
        confidence: Any? = null,
        symptoms: String = "",
        causes: String = "",
        fertilizer: String = "",
        pesticides: String = "",
        preventionTips: String = "",
        irrigationTips: String = "",
        description: String = "",
        timestamp: Long = System.currentTimeMillis(),
        imageUrl: String? = null,
        isCropIdentified: Boolean = true
    ) {
        this.id = id
        this.cropName = cropName
        this.diseaseName = diseaseName
        this.status = status
        this.confidence = confidence
        this.symptoms = symptoms
        this.causes = causes
        this.fertilizer = fertilizer
        this.pesticides = pesticides
        this.preventionTips = preventionTips
        this.irrigationTips = irrigationTips
        this.description = description
        this.timestamp = timestamp
        this.imageUrl = imageUrl
        this.isCropIdentified = isCropIdentified
    }

    /**
     * Requirement 7: Safely converts the confidence value to a float (0.0 to 1.0).
     * Handles String (e.g., "85%"), Double, Float, Long, and Integer types.
     */
    fun getConfidenceValue(): Float {
        val value = when (val v = confidence) {
            is Number -> v.toFloat()
            is String -> {
                try {
                    v.replace("%", "").trim().toFloat()
                } catch (e: Exception) {
                    0f
                }
            }
            else -> 0f
        }
        // Normalize: if value is > 1.0, assume it's a percentage and convert to decimal
        return if (value > 1.0f) value / 100f else value
    }

    /**
     * Helper to get a formatted confidence string (e.g., "85.0%") for UI display.
     */
    fun getConfidenceString(): String {
        val value = getConfidenceValue()
        return if (value <= 0 && confidence != null && confidence.toString() == "N/A") "N/A"
        else String.format(Locale.US, "%.1f%%", value * 100)
    }
}
