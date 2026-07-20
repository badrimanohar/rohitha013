package com.example.agriguard.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.io.File;

/**
 * Page Object Model for AgriGuard Web Portal Disease Detection & AI Scanner Page.
 */
public class DiseaseDetectionPage extends BasePage {
    private final By fileUploadInput = By.id("cropImageUpload");
    private final By analyzeButton = By.id("btnAnalyzeCrop");
    private final By resultCard = By.id("detectionResultCard");
    private final By cropNameLabel = By.id("lblCropName");
    private final By diseaseStatusLabel = By.id("lblDiseaseStatus");
    private final By confidenceBadge = By.id("lblConfidence");
    private final By treatmentSection = By.id("treatmentRecommendations");

    public DiseaseDetectionPage(WebDriver driver) {
        super(driver);
    }

    public void uploadCropImage(File imageFile) {
        driver.findElement(fileUploadInput).sendKeys(imageFile.getAbsolutePath());
    }

    public void clickAnalyze() {
        click(analyzeButton);
    }

    public boolean isResultCardDisplayed() {
        return waitForElement(resultCard).isDisplayed();
    }

    public String getCropName() {
        return getText(cropNameLabel);
    }

    public String getDiseaseStatus() {
        return getText(diseaseStatusLabel);
    }

    public String getConfidence() {
        return getText(confidenceBadge);
    }

    public boolean hasTreatmentRecommendations() {
        return isElementPresent(treatmentSection);
    }
}
