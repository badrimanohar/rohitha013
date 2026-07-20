package com.example.agriguard.models

import com.example.agriguard.api.CropRequest
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive Unit Tests for AgriGuard data models.
 * Covers: User Profile, Disease Detection Results, Community Chat & Posts, and Notifications.
 */
class ModelUnitTests {

    @Test
    fun testUserConstructorAndGetters() {
        val timestamp = System.currentTimeMillis()
        val user = User("Manohar Badri", "manohar@agriguard.com", "https://photo.url", "google", timestamp)

        assertEquals("Manohar Badri", user.name)
        assertEquals("manohar@agriguard.com", user.email)
        assertEquals("https://photo.url", user.photoUrl)
        assertEquals("google", user.authProvider)
        assertEquals(timestamp, user.createdAt)
    }

    @Test
    fun testUserEmptyConstructorForFirebase() {
        val user = User()
        assertNull(user.name)
        assertNull(user.email)
        assertEquals(0L, user.createdAt)
    }

    @Test
    fun testPredictionResultDefaultsAndSetters() {
        val prediction = PredictionResult(
            id = "pred_123",
            cropName = "Tomato",
            diseaseName = "Early Blight",
            status = "Diseased",
            confidence = "94.5%",
            description = "Fungal infection affecting leaves and stems.",
            preventionTips = "Rotate crops and apply copper fungicide.",
            fertilizer = "Phosphorus rich fertilizer",
            pesticides = "Chlorothalonil",
            causes = "Alternaria solani fungus",
            symptoms = "Dark concentric rings on older leaves",
            isCropIdentified = true,
            imageUrl = "https://storage.googleapis.com/agriguard/img1.jpg"
        )

        assertEquals("pred_123", prediction.id)
        assertEquals("Tomato", prediction.cropName)
        assertEquals("Early Blight", prediction.diseaseName)
        assertEquals("Diseased", prediction.status)
        assertEquals("94.5%", prediction.confidence)
        assertTrue(prediction.isCropIdentified)
        assertNotNull(prediction.timestamp)
    }

    @Test
    fun testPredictionResultHealthyStatus() {
        val healthyResult = PredictionResult(
            status = "Healthy",
            cropName = "Rice",
            diseaseName = "None",
            confidence = "99.1%"
        )

        assertEquals("Healthy", healthyResult.status)
        assertEquals("None", healthyResult.diseaseName)
    }

    @Test
    fun testCommunityModel() {
        val community = Community(
            "comm_001",
            "Rice Farmers Guild",
            "Discussing paddy cultivation and water management",
            1250,
            true
        )

        assertEquals("comm_001", community.getId())
        assertEquals("Rice Farmers Guild", community.getName())
        assertEquals(1250, community.getMemberCount())
        assertTrue(community.isJoined())

        community.setJoined(false)
        assertFalse(community.isJoined())
    }

    @Test
    fun testCommunityPostModel() {
        val post = CommunityPost(
            "post_01",
            "user_88",
            "Rohitha",
            "How often should I irrigate during vegetative stage?",
            "https://img.url/paddy.jpg",
            System.currentTimeMillis(),
            14
        )

        assertEquals("post_01", post.getPostId())
        assertEquals("Rohitha", post.getAuthorName())
        assertEquals("How often should I irrigate during vegetative stage?", post.getContent())
        assertEquals(14, post.getLikesCount())
    }

    @Test
    fun testChatMessageModel() {
        val chat = ChatMessage(
            "msg_101",
            "user_42",
            "Hello everyone, weather looks good for harvest!",
            System.currentTimeMillis(),
            true
        )

        assertEquals("msg_101", chat.getMessageId())
        assertEquals("user_42", chat.getSenderId())
        assertEquals("Hello everyone, weather looks good for harvest!", chat.getMessage())
        assertTrue(chat.isSentByMe())
    }

    @Test
    fun testFeatureModel() {
        val feature = Feature(
            "Crop Disease Detection",
            "Upload image to identify diseases",
            101,
            "Scan Crop"
        )

        assertEquals("Crop Disease Detection", feature.getTitle())
        assertEquals("Upload image to identify diseases", feature.getDescription())
        assertEquals("Scan Crop", feature.getActionText())
    }

    @Test
    fun testNotificationModel() {
        val notification = Notification(
            "notif_55",
            "Severe Weather Alert",
            "Heavy rainfall expected over next 48 hours in your district.",
            System.currentTimeMillis(),
            false
        )

        assertEquals("notif_55", notification.getId())
        assertEquals("Severe Weather Alert", notification.getTitle())
        assertFalse(notification.isRead())

        notification.setRead(true)
        assertTrue(notification.isRead())
    }

    @Test
    fun testCropRequestModel() {
        val request = CropRequest(
            images = listOf("base64_encoded_string_sample"),
            latitude = 16.5062,
            longitude = 80.6480,
            similarImages = true
        )

        assertEquals(1, request.images.size)
        assertEquals("base64_encoded_string_sample", request.images[0])
        assertEquals(16.5062, request.latitude)
        assertEquals(80.6480, request.longitude)
        assertTrue(request.similarImages)
    }
}
