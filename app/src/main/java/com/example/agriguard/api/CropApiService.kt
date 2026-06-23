package com.example.agriguard.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface CropApiService {
    /**
     * Kindwise (Plant.id) Identification & Health Assessment endpoint.
     * Requests full disease details including treatment and symptoms.
     */
    @POST("identification")
    suspend fun identifyCrop(
        @Header("Api-Key") apiKey: String,
        @Body request: CropRequest,
        @Query("language") language: String = "en",
        @Query("details") details: String = "description,treatment,cause,common_names,url,symptoms"
    ): Response<PlantResponse>
}
