package com.example.agriguard.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object Model for AgriGuard Web Portal Login Page.
 */
public class LoginPage extends BasePage {
    private final By emailInput = By.id("emailInput");
    private final By passwordInput = By.id("passwordInput");
    private final By loginButton = By.id("loginButton");
    private final By errorMessage = By.id("loginErrorMessage");
    private final By googleSignInButton = By.id("googleSignInButton");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void enterEmail(String email) {
        type(emailInput, email);
    }

    public void enterPassword(String password) {
        type(passwordInput, password);
    }

    public DashboardPage clickLogin() {
        click(loginButton);
        return new DashboardPage(driver);
    }

    public DashboardPage loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        return clickLogin();
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}
