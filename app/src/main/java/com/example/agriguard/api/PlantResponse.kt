package com.example.agriguard.api

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from Kindwise (Plant.id) API.
 * Updated to match the latest API structure for robust disease detection.
 */
data class PlantResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("model_version") val modelVersion: String? = null,
    @SerializedName("result") val result: PlantResult? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("error") val error: ApiError? = null
)

/**
 * Main result block containing all detection sub-blocks.
 */
data class PlantResult(
    @SerializedName("is_plant") val isPlant: IsPlant? = null,
    @SerializedName("classification") val classification: Classification? = null,
    @SerializedName("disease") val disease: DiseaseResult? = null,
    @SerializedName("is_healthy") val isHealthy: IsHealthy? = null,
    @SerializedName("health_assessment") val healthAssessment: HealthAssessment? = null
)

/**
 * Validation for plant presence.
 */
data class IsPlant(
    @SerializedName("probability") val probability: Double? = null,
    @SerializedName("binary") val binary: Boolean? = null
)

/**
 * Direct health status block.
 */
data class IsHealthy(
    @SerializedName("probability") val probability: Double? = null,
    @SerializedName("binary") val binary: Boolean? = null
)

/**
 * Advanced assessment block (legacy or alternative depending on API version).
 */
data class HealthAssessment(
    @SerializedName("is_healthy") val isHealthy: Boolean? = null,
    @SerializedName("is_healthy_probability") val isHealthyProbability: Double? = null,
    @SerializedName("diseases") val diseases: List<Suggestion>? = null
)

/**
 * Standard API error structure.
 */
data class ApiError(
    @SerializedName("message") val message: String? = null
)
