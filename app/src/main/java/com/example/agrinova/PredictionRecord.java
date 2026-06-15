package com.example.agrinova;

import java.util.HashMap;
import java.util.Map;

public class PredictionRecord {
    private String id;
    private String userId;
    private String cropName;
    private String diseaseName;
    private String confidence;
    private String qualityGrade;
    private String marketPrice;
    private String imageUrl;
    private long timestamp;
    private String feedback;

    public PredictionRecord() {
        // Required for Firestore
    }

    public PredictionRecord(String id, String userId, String cropName, String diseaseName, String confidence, 
                            String qualityGrade, String marketPrice, String imageUrl, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.cropName = cropName;
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.qualityGrade = qualityGrade;
        this.marketPrice = marketPrice;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
    public String getMarketPrice() { return marketPrice; }
    public void setMarketPrice(String marketPrice) { this.marketPrice = marketPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("cropName", cropName);
        map.put("diseaseName", diseaseName);
        map.put("confidence", confidence);
        map.put("qualityGrade", qualityGrade);
        map.put("marketPrice", marketPrice);
        map.put("imageUrl", imageUrl);
        map.put("timestamp", timestamp);
        map.put("feedback", feedback);
        return map;
    }
}
