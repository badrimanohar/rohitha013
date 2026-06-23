package com.example.agriguard.api

import com.google.gson.annotations.SerializedName

/**
 * Detailed information about a plant disease or variety.
 * Mapped to Crop.Health (Kindwise) response for maximum data extraction.
 */
data class DiseaseDetails(
    @SerializedName("description") val description: String? = null,
    @SerializedName("treatment") val treatment: Treatment? = null,
    @SerializedName("cause") val cause: String? = null,
    @SerializedName("symptoms") val symptoms: Symptoms? = null,
    @SerializedName("common_names") val commonNames: List<String>? = null,
    @SerializedName("url") val url: String? = null
)

/**
 * Flexible Symptoms class to handle both text and object responses.
 */
data class Symptoms(
    @SerializedName("text") val text: String? = null,
    @SerializedName("description") val description: String? = null
)

/**
 * Treatment recommendations broken down by category.
 */
data class Treatment(
    @SerializedName("chemical") val chemical: List<String>? = null,
    @SerializedName("biological") val biological: List<String>? = null,
    @SerializedName("prevention") val prevention: List<String>? = null
)
