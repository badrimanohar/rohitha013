package com.example.agrinova;

import java.io.Serializable;

public class Crop implements Serializable {
    public String name;
    public String scientificName;
    public String type;
    public String category; // Fruits, Vegetables, Grains, Pulses, Cash Crops
    public String description;
    public String imageResource; // For now, we'll use resource names or placeholder logic
    
    // A. Basic Info
    public String season;
    public String duration;

    // B. Soil
    public String soilType;
    public String soilPh;
    public String drainage;
    public String soilPreparation;

    // C. Climate
    public String idealTemp;
    public String humidity;
    public String rainfall;
    public String suitableClimate;

    // D. Seed
    public String seedVarieties;
    public String seedQuantity;
    public String seedTreatment;
    public String sowingDepth;

    // E. Sowing
    public String sowingMonths;
    public String sowingProcedure;
    public String germinationTime;

    // F. Fertilizer
    public String organicFertilizer;
    public String chemicalFertilizer;
    public String fertilizerTimetable;
    public String npkRatio;
    public String fertilizerQuantity;

    // G. Irrigation
    public String waterRequirement;
    public String irrigationMethods;
    public String irrigationSchedule;
    public String criticalStages;

    // H. Disease
    public String commonDiseases;
    public String diseaseSymptoms;
    public String diseasePrevention;
    public String recommendedPesticides;

    // I. Pests
    public String commonPests;
    public String pestSymptoms;
    public String organicPestControl;
    public String chemicalPestControl;

    // J. Harvest
    public String harvestDuration;
    public String harvestingMethods;
    public String maturitySigns;
    public String yieldPerAcre;

    // K. Market
    public String marketPrice;
    public String sellingMarkets;
    public String demandTrends;
    public String storageMethods;

    // L. Profit
    public String investment;
    public String expectedIncome;
    public String profitEstimation;
    public String riskFactors;

    public Crop() {}
}
