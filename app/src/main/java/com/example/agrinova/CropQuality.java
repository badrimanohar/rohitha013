package com.example.agrinova;

import java.util.HashMap;
import java.util.Map;

public class CropQuality {
    private String id;
    private String cropName;
    private String qualityGrade;
    private String marketPrice;
    private String qualityPercentage;
    private String userEmail;
    private String userId;
    private String timestamp;

    public CropQuality() {
        // Required for Firebase
    }

    public CropQuality(String id, String cropName, String qualityGrade, String marketPrice, String qualityPercentage, String userEmail, String userId, String timestamp) {
        this.id = id;
        this.cropName = cropName;
        this.qualityGrade = qualityGrade;
        this.marketPrice = marketPrice;
        this.qualityPercentage = qualityPercentage;
        this.userEmail = userEmail;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getCropName() { return cropName; }
    public String getQualityGrade() { return qualityGrade; }
    public String getMarketPrice() { return marketPrice; }
    public String getQualityPercentage() { return qualityPercentage; }
    public String getUserEmail() { return userEmail; }
    public String getUserId() { return userId; }
    public String getTimestamp() { return timestamp; }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("cropName", cropName);
        map.put("qualityGrade", qualityGrade);
        map.put("marketPrice", marketPrice);
        map.put("qualityPercentage", qualityPercentage);
        map.put("userEmail", userEmail);
        map.put("userId", userId);
        map.put("timestamp", timestamp);
        return map;
    }
}
