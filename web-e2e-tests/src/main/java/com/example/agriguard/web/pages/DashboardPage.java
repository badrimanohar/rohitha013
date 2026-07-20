package com.example.agriguard.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object Model for AgriGuard Web Portal Dashboard Page.
 */
public class DashboardPage extends BasePage {
    private final By navDiseaseDetection = By.id("navDiseaseDetection");
    private final By navMarketPrices = By.id("navMarketPrices");
    private final By navCommunity = By.id("navCommunity");
    private final By userProfileMenu = By.id("userProfileMenu");
    private final By logoutOption = By.id("logoutOption");
    private final By welcomeHeader = By.cssSelector("h1.welcome-header");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public String getWelcomeHeader() {
        return getText(welcomeHeader);
    }

    public DiseaseDetectionPage navigateToDiseaseDetection() {
        click(navDiseaseDetection);
        return new DiseaseDetectionPage(driver);
    }

    public void navigateToMarketPrices() {
        click(navMarketPrices);
    }

    public void navigateToCommunity() {
        click(navCommunity);
    }

    public LoginPage logout() {
        click(userProfileMenu);
        click(logoutOption);
        return new LoginPage(driver);
    }
}
