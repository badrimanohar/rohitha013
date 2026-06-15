package com.example.agrinova;

import java.util.HashMap;
import java.util.Map;

public class CropDetection {
    private String id;
    private String cropName;
    private String diseaseName;
    private String treatment;
    private String userEmail;
    private String userId;
    private String timestamp;

    public CropDetection() {
        // Required for Firebase
    }

    public CropDetection(String id, String cropName, String diseaseName, String treatment, String userEmail, String userId, String timestamp) {
        this.id = id;
        this.cropName = cropName;
        this.diseaseName = diseaseName;
        this.treatment = treatment;
        this.userEmail = userEmail;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getCropName() { return cropName; }
    public String getDiseaseName() { return diseaseName; }
    public String getTreatment() { return treatment; }
    public String getUserEmail() { return userEmail; }
    public String getUserId() { return userId; }
    public String getTimestamp() { return timestamp; }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("cropName", cropName);
        result.put("diseaseName", diseaseName);
        result.put("treatment", treatment);
        result.put("userEmail", userEmail);
        result.put("userId", userId);
        result.put("timestamp", timestamp);
        return result;
    }
}
