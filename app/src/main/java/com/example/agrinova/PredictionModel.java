package com.example.agrinova;

import java.io.Serializable;

public class PredictionModel implements Serializable {
    private String predictionId;
    private String state;
    private String district;
    private String market;
    private String commodity;
    private String variety;
    private double predictedPrice;
    private String predictionDate;
    private long timestamp;

    public PredictionModel() {
        // Required for Firebase
    }

    public PredictionModel(String predictionId, String state, String district, String market, 
                           String commodity, String variety, double predictedPrice, 
                           String predictionDate, long timestamp) {
        this.predictionId = predictionId;
        this.state = state;
        this.district = district;
        this.market = market;
        this.commodity = commodity;
        this.variety = variety;
        this.predictedPrice = predictedPrice;
        this.predictionDate = predictionDate;
        this.timestamp = timestamp;
    }

    public String getPredictionId() { return predictionId; }
    public void setPredictionId(String predictionId) { this.predictionId = predictionId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public String getCommodity() { return commodity; }
    public void setCommodity(String commodity) { this.commodity = commodity; }

    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }

    public double getPredictedPrice() { return predictedPrice; }
    public void setPredictedPrice(double predictedPrice) { this.predictedPrice = predictedPrice; }

    public String getPredictionDate() { return predictionDate; }
    public void setPredictionDate(String predictionDate) { this.predictionDate = predictionDate; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
