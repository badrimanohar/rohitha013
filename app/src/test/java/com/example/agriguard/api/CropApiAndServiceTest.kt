package com.example.agriguard.api

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Automated API & Backend Integration Tests for AgriGuard.
 * Tests endpoints, authentication, authorization, successful detection, error responses, and invalid inputs.
 */
class CropApiAndServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: CropApiService
    private val gson = Gson()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CropApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testAuthenticationApiKeyHeader() = runBlocking {
        val mockJson = """
            {
              "status": "COMPLETED",
              "result": {
                "is_plant": { "binary": true, "probability": 0.99 },
                "is_healthy": { "binary": false, "probability": 0.12 }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        val request = CropRequest(listOf("base64_sample_image"))
        val response = apiService.identifyCrop("TEST_API_KEY_12345", request)

        assertTrue(response.isSuccessful)
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("TEST_API_KEY_12345", recordedRequest.getHeader("Api-Key"))
        assertEquals("/identification?language=en&details=description%2Ctreatment%2Ccause%2Ccommon_names%2Curl%2Csymptoms", recordedRequest.path)
    }

    @Test
    fun testUnauthorizedError401() = runBlocking {
        val errorJson = """{ "error": { "message": "Invalid API Key" } }"""
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(errorJson))

        val request = CropRequest(listOf("base64_sample_image"))
        val response = apiService.identifyCrop("INVALID_KEY", request)

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
    }

    @Test
    fun testSuccessfulCropDetectionAndDiseaseParsing() = runBlocking {
        val fullResponseJson = """
            {
              "access_token": "token_abc123",
              "model_version": "v1.5",
              "status": "COMPLETED",
              "result": {
                "is_plant": { "probability": 0.985, "binary": true },
                "is_healthy": { "probability": 0.05, "binary": false },
                "disease": {
                  "suggestions": [
                    {
                      "id": "sug_01",
                      "name": "Late Blight",
                      "probability": 0.92,
                      "details": {
                        "description": "Severe fungal infection causing leaf necrosis.",
                        "treatment": {
                          "chemical": ["Mancozeb", "Chlorothalonil"],
                          "biological": ["Bacillus subtilis"],
                          "prevention": ["Use certified disease-free seeds"]
                        }
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(fullResponseJson))

        val request = CropRequest(listOf("base64_image_data"), latitude = 16.5, longitude = 80.6)
        val response = apiService.identifyCrop("VALID_API_KEY", request)

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals("COMPLETED", body?.status)
        assertEquals(true, body?.result?.isPlant?.binary)
        assertEquals(false, body?.result?.isHealthy?.binary)
        
        val diseaseList = body?.result?.disease?.suggestions
        assertNotNull(diseaseList)
        assertEquals(1, diseaseList?.size)
        assertEquals("Late Blight", diseaseList?.get(0)?.name)
        assertEquals(0.92, diseaseList?.get(0)?.probability!!, 0.001)
        
        val treatment = diseaseList[0].details?.treatment
        assertNotNull(treatment)
        assertEquals(2, treatment?.chemical?.size)
        assertEquals("Mancozeb", treatment?.chemical?.get(0))
    }

    @Test
    fun testHealthyPlantResponse() = runBlocking {
        val healthyJson = """
            {
              "status": "COMPLETED",
              "result": {
                "is_plant": { "probability": 0.999, "binary": true },
                "is_healthy": { "probability": 0.965, "binary": true }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(healthyJson))

        val response = apiService.identifyCrop("VALID_API_KEY", CropRequest(listOf("base64")))
        assertTrue(response.isSuccessful)
        assertTrue(response.body()?.result?.isHealthy?.binary == true)
    }

    @Test
    fun testNotAPlantResponse() = runBlocking {
        val notPlantJson = """
            {
              "status": "COMPLETED",
              "result": {
                "is_plant": { "probability": 0.12, "binary": false }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(notPlantJson))

        val response = apiService.identifyCrop("VALID_API_KEY", CropRequest(listOf("base64")))
        assertTrue(response.isSuccessful)
        assertEquals(false, response.body()?.result?.isPlant?.binary)
    }

    @Test
    fun testBadRequestError400() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request Payload"))
        val response = apiService.identifyCrop("VALID_API_KEY", CropRequest(emptyList()))
        assertEquals(400, response.code())
    }

    @Test
    fun testInternalServerError500() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))
        val response = apiService.identifyCrop("VALID_API_KEY", CropRequest(listOf("img")))
        assertEquals(500, response.code())
    }

    @Test
    fun testMalformedJsonResponseHandling() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{ invalid_json: ... [ "))
        
        try {
            apiService.identifyCrop("VALID_API_KEY", CropRequest(listOf("img")))
            fail("Should throw JsonSyntaxException or MalformedJsonException")
        } catch (e: Exception) {
            assertTrue(e.javaClass.name.contains("Json") || e.javaClass.name.contains("Malformed"))
        }
    }
}
