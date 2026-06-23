import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def wait_for_element(driver, locator, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located(locator)
    )

def test_splash_screen_redirect(driver):
    """
    Verify SplashActivity correctly launches and redirects to LoginActivity.
    """
    # Wait for the login screen fields as SplashActivity automatically finishes and transitions
    email_field = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etEmail"), timeout=15)
    assert email_field is not None, "Failed to redirect from Splash screen to LoginActivity"

def test_login_validation_invalid_email(driver):
    """
    Verify validation error is displayed for an invalid email format.
    """
    email_field = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etEmail"))
    password_field = driver.find_element(By.ID, "com.example.agriguard:id/etPassword")
    login_btn = driver.find_element(By.ID, "com.example.agriguard:id/btnLogin")
    
    # Enter invalid email
    email_field.send_keys("invalid_email")
    password_field.send_keys("Password123!")
    login_btn.click()
    
    # Check for TextInputLayout error for email field
    email_layout = driver.find_element(By.ID, "com.example.agriguard:id/tilEmail")
    error_text = email_layout.get_attribute("error")
    assert error_text == "Invalid email format" or error_text is not None

def test_login_validation_empty_password(driver):
    """
    Verify error is displayed when trying to log in with an empty password.
    """
    email_field = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etEmail"))
    login_btn = driver.find_element(By.ID, "com.example.agriguard:id/btnLogin")
    
    email_field.send_keys("farmer@agriguard.com")
    login_btn.click()
    
    password_layout = driver.find_element(By.ID, "com.example.agriguard:id/tilPassword")
    error_text = password_layout.get_attribute("error")
    assert error_text == "Password required" or error_text is not None

def test_navigation_to_signup(driver):
    """
    Verify user can navigate from Login screen to SignUp screen.
    """
    goto_signup_link = wait_for_element(driver, (By.ID, "com.example.agriguard:id/tvGoToSignup"))
    goto_signup_link.click()
    
    # Verify we are on SignUpActivity by checking for name field
    name_field = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etName"))
    assert name_field is not None, "Failed to navigate to SignUp screen"

def test_navigation_to_forgot_password(driver):
    """
    Verify user can navigate to Forgot Password screen.
    """
    forgot_password_link = wait_for_element(driver, (By.ID, "com.example.agriguard:id/tvForgotPassword"))
    forgot_password_link.click()
    
    # Verify we are on ForgotPasswordActivity
    btn_reset = wait_for_element(driver, (By.ID, "com.example.agriguard:id/btnResetPassword"))
    assert btn_reset is not None, "Failed to navigate to Forgot Password screen"
