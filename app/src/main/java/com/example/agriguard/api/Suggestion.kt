package com.example.agriguard.api

import com.google.gson.annotations.SerializedName

/**
 * Generic suggestion model for both plant identification and disease detection.
 */
data class Suggestion(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("probability") val probability: Double? = null,
    @SerializedName("details") val details: DiseaseDetails? = null
)

/**
 * Container for plant classification suggestions.
 */
data class Classification(
    @SerializedName("suggestions") val suggestions: List<Suggestion>? = null
)

/**
 * Container for disease detection suggestions.
 */
data class DiseaseResult(
    @SerializedName("suggestions") val suggestions: List<Suggestion>? = null
)
