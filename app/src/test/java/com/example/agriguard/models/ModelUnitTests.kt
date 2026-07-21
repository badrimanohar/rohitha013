package com.example.agriguard.models

import com.example.agriguard.api.*
import com.example.agriguard.utils.LocationData
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive Unit Tests for AgriGuard data models and API response models.
 * Covers: User Profile, Disease Detection Results, Community Chat & Posts, Notifications,
 * API Requests/Responses, and LocationData utility.
 */
class ModelUnitTests {

    @Test
    fun testUserConstructorAndGetters() {
        val timestamp = System.currentTimeMillis()
        val user = User("Manohar Badri", "manohar@agriguard.com", "https://photo.url", "google", timestamp)

        assertEquals("Manohar Badri", user.name)
        assertEquals("manohar@agriguard.com", user.email)
        assertEquals("https://photo.url", user.profileImage)
        assertEquals("google", user.provider)
        assertEquals(timestamp, user.createdAt)
        assertEquals(timestamp, user.updatedAt)
    }

    @Test
    fun testUserEmptyConstructorAndSetters() {
        val user = User()
        assertNull(user.name)
        assertNull(user.email)
        assertEquals(0L, user.createdAt)

        user.name = "Updated Farmer"
        user.email = "updated@agriguard.com"
        user.profileImage = "https://new.photo.url"
        user.provider = "password"
        user.phone = "+919876543210"
        user.location = "Guntur, Andhra Pradesh"
        user.bio = "Passionate organic farmer"
        user.preferredCrops = "Rice, Chilli, Cotton"

        assertEquals("Updated Farmer", user.name)
        assertEquals("updated@agriguard.com", user.email)
        assertEquals("https://new.photo.url", user.profileImage)
        assertEquals("password", user.provider)
        assertEquals("+919876543210", user.phone)
        assertEquals("Guntur, Andhra Pradesh", user.location)
        assertEquals("Passionate organic farmer", user.bio)
        assertEquals("Rice, Chilli, Cotton", user.preferredCrops)
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
        assertEquals("Fungal infection affecting leaves and stems.", prediction.description)
        assertEquals("Rotate crops and apply copper fungicide.", prediction.preventionTips)
        assertEquals("Phosphorus rich fertilizer", prediction.fertilizer)
        assertEquals("Chlorothalonil", prediction.pesticides)
        assertEquals("Alternaria solani fungus", prediction.causes)
        assertEquals("Dark concentric rings on older leaves", prediction.symptoms)
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
        assertEquals("Rice", healthyResult.cropName)
        assertEquals("99.1%", healthyResult.confidence)
    }

    @Test
    fun testCommunityModelConstructors() {
        val community = Community(
            "comm_001",
            "Rice Farmers Guild",
            "Discussing paddy cultivation and water management",
            1250,
            "https://image.url/rice.jpg"
        )

        assertEquals("comm_001", community.id)
        assertEquals("Rice Farmers Guild", community.name)
        assertEquals("Discussing paddy cultivation and water management", community.description)
        assertEquals(1250, community.memberCount)
        assertEquals("https://image.url/rice.jpg", community.image)

        community.isJoined = true
        assertTrue(community.isJoined)

        community.isJoined = false
        assertFalse(community.isJoined)
    }

    @Test
    fun testCommunityModelFullConstructor() {
        val community = Community(
            "comm_002",
            "Cotton Growers Hub",
            "Pest management and yield optimization",
            850,
            "Check out the new fertilizer pricing!",
            3,
            "https://image.url/cotton.jpg",
            true
        )

        assertEquals("comm_002", community.id)
        assertEquals("Cotton Growers Hub", community.name)
        assertEquals("Check out the new fertilizer pricing!", community.lastMessage)
        assertEquals(3, community.unreadCount)
        assertTrue(community.isJoined)
    }

    @Test
    fun testCommunityPostModel() {
        val post = CommunityPost(
            "Rohitha",
            "2 hours ago",
            "How often should I irrigate during vegetative stage?",
            101,
            14,
            5
        )

        assertEquals("Rohitha", post.author)
        assertEquals("2 hours ago", post.time)
        assertEquals("How often should I irrigate during vegetative stage?", post.content)
        assertEquals(101, post.imageResId)
        assertEquals(14, post.likes)
        assertEquals(5, post.comments)
    }

    @Test
    fun testChatMessageModel() {
        val timestamp = System.currentTimeMillis()
        val chat = ChatMessage(
            "msg_101",
            "user_42",
            "Manohar Badri",
            "Hello everyone, weather looks good for harvest!",
            "https://image.url/chat.jpg",
            timestamp
        )

        assertEquals("msg_101", chat.id)
        assertEquals("user_42", chat.senderId)
        assertEquals("Manohar Badri", chat.senderName)
        assertEquals("Hello everyone, weather looks good for harvest!", chat.text)
        assertEquals("https://image.url/chat.jpg", chat.image)
        assertEquals(timestamp, chat.timestamp)
    }

    @Test
    fun testChatMessageEmptyConstructor() {
        val chat = ChatMessage()
        assertNull(chat.id)
        assertNull(chat.senderId)
        assertNull(chat.text)
        assertEquals(0L, chat.timestamp)
    }

    @Test
    fun testFeatureModel() {
        val feature = Feature(
            "Crop Disease Detection",
            "Upload image to identify diseases",
            101,
            "Scan Crop"
        )

        assertEquals("Crop Disease Detection", feature.title)
        assertEquals("Upload image to identify diseases", feature.description)
        assertEquals(101, feature.iconRes)
        assertEquals("Scan Crop", feature.buttonText)
    }

    @Test
    fun testNotificationModel() {
        val timestamp = System.currentTimeMillis()
        val notification = Notification(
            "notif_55",
            "Severe Weather Alert",
            "Heavy rainfall expected over next 48 hours in your district.",
            timestamp,
            false
        )

        assertEquals("notif_55", notification.id)
        assertEquals("Severe Weather Alert", notification.title)
        assertEquals("Heavy rainfall expected over next 48 hours in your district.", notification.message)
        assertEquals(timestamp, notification.timestamp)
        assertFalse(notification.isRead)

        notification.isRead = true
        assertTrue(notification.isRead)
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

    @Test
    fun testPlantResponseAndSubModels() {
        val suggestion = Suggestion(
            id = "disease_01",
            name = "Alternaria solani",
            probability = 0.954,
            details = DiseaseDetails(
                description = "Early blight caused by fungus.",
                treatment = Treatment(
                    chemical = listOf("Copper Fungicide", "Mancozeb"),
                    biological = listOf("Bacillus subtilis"),
                    prevention = listOf("Crop rotation", "Drip irrigation")
                ),
                cause = "Alternaria solani fungus",
                symptoms = Symptoms(
                    text = "Concentric rings on leaves",
                    description = "Starts on lower foliage and moves up."
                ),
                commonNames = listOf("Early blight", "Target spot"),
                url = "https://en.wikipedia.org/wiki/Alternaria_solani"
            )
        )

        val response = PlantResponse(
            accessToken = "token_xyz",
            modelVersion = "v2.1",
            result = PlantResult(
                isPlant = IsPlant(probability = 0.99, binary = true),
                classification = Classification(suggestions = listOf(suggestion)),
                disease = DiseaseResult(suggestions = listOf(suggestion)),
                isHealthy = IsHealthy(probability = 0.046, binary = false),
                healthAssessment = HealthAssessment(
                    isHealthy = false,
                    isHealthyProbability = 0.046,
                    diseases = listOf(suggestion)
                )
            ),
            status = "COMPLETED"
        )

        assertEquals("token_xyz", response.accessToken)
        assertEquals("v2.1", response.modelVersion)
        assertEquals("COMPLETED", response.status)
        assertNotNull(response.result)

        val result = response.result!!
        assertEquals(true, result.isPlant?.binary)
        assertEquals(0.99, result.isPlant?.probability)
        assertEquals(false, result.isHealthy?.binary)

        val firstDisease = result.disease?.suggestions?.first()
        assertNotNull(firstDisease)
        assertEquals("Alternaria solani", firstDisease?.name)
        assertEquals(0.954, firstDisease?.probability)

        val details = firstDisease?.details
        assertNotNull(details)
        assertEquals("Alternaria solani fungus", details?.cause)
        assertEquals(2, details?.treatment?.chemical?.size)
        assertEquals("Copper Fungicide", details?.treatment?.chemical?.get(0))
        assertEquals("Concentric rings on leaves", details?.symptoms?.text)
        assertEquals("Early blight", details?.commonNames?.get(0))
    }

    @Test
    fun testLocationDataStatesAndDistricts() {
        val states = LocationData.getStates()
        assertNotNull(states)
        assertTrue(states.contains("Andhra Pradesh"))
        assertTrue(states.contains("Maharashtra"))
        assertTrue(states.contains("Punjab"))

        val apDistricts = LocationData.getDistricts("Andhra Pradesh")
        assertNotNull(apDistricts)
        assertTrue(apDistricts.contains("Guntur"))
        assertTrue(apDistricts.contains("Krishna"))
        assertTrue(apDistricts.contains("Anantapur"))

        val puneMarkets = LocationData.getMarkets("Pune")
        assertNotNull(puneMarkets)
        assertTrue(puneMarkets.contains("Gultekdi APMC"))
    }
}

