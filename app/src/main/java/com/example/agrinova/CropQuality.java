package com.example.agrinova;

import java.util.HashMap;
import java.util.Map;

public class CropQuality {
    public String id;
    public String crop;
    public String grade;
    public String price;
    public String percentage;
    public String disease;
    public String recommendation;
    public String userEmail;
    public String userId;
    public String timestamp;

    public CropQuality() {
        // Required for Firebase
    }

    public CropQuality(String id, String crop, String grade, String price, String percentage, String disease, String recommendation, String userEmail, String userId, String timestamp) {
        this.id = id;
        this.crop = crop;
        this.grade = grade;
        this.price = price;
        this.percentage = percentage;
        this.disease = disease;
        this.recommendation = recommendation;
        this.userEmail = userEmail;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters for compatibility with existing code
    public String getCropName() { return crop; }
    public String getQualityGrade() { return grade; }
    public String getMarketPrice() { return price; }
    public String getQualityPercentage() { return percentage; }
    public String getTimestamp() { return timestamp; }
    public String getDisease() { return disease; }
    public String getRecommendation() { return recommendation; }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("crop", crop);
        result.put("grade", grade);
        result.put("price", price);
        result.put("percentage", percentage);
        result.put("disease", disease);
        result.put("recommendation", recommendation);
        result.put("userEmail", userEmail);
        result.put("userId", userId);
        result.put("timestamp", timestamp);
        return result;
    }
}
