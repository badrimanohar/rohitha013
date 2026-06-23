package com.example.agriguard.api

import com.google.gson.annotations.SerializedName

data class CropRequest(
    @SerializedName("images") val images: List<String>,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("similar_images") val similarImages: Boolean = true
)
