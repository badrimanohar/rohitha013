import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def wait_for_element(driver, locator, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located(locator)
    )

def login_helper(driver):
    """Logs in before starting dashboard navigation tests."""
    email_field = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etEmail"))
    password_field = driver.find_element(By.ID, "com.example.agriguard:id/etPassword")
    login_btn = driver.find_element(By.ID, "com.example.agriguard:id/btnLogin")
    
    email_field.send_keys("farmer@agriguard.com")
    password_field.send_keys("Password123!")
    login_btn.click()
    
    # Wait for dashboard to load
    wait_for_element(driver, (By.ID, "com.example.agriguard:id/bottom_navigation"))

def test_bottom_navigation_tabs(driver):
    """
    Verify navigating through all BottomNavigationView tabs loads respective fragments.
    """
    login_helper(driver)
    
    # Click Disease Detection tab
    detect_tab = driver.find_element(By.ID, "com.example.agriguard:id/nav_detect")
    detect_tab.click()
    # Verify elements inside DiseaseFragment are loaded (e.g. camera button)
    btn_camera = wait_for_element(driver, (By.ID, "com.example.agriguard:id/btnCamera"))
    assert btn_camera is not None
    
    # Click Community tab
    community_tab = driver.find_element(By.ID, "com.example.agriguard:id/nav_community")
    community_tab.click()
    # Verify post creation/search inside CommunityFragment is visible
    btn_create_post = wait_for_element(driver, (By.ID, "com.example.agriguard:id/fabCreatePost"))
    assert btn_create_post is not None

    # Click Prices tab
    prices_tab = driver.find_element(By.ID, "com.example.agriguard:id/nav_prices")
    prices_tab.click()
    # Verify crop search/spinner inside PricesFragment
    search_crop = wait_for_element(driver, (By.ID, "com.example.agriguard:id/svCropSearch"))
    assert search_crop is not None

    # Click Profile tab
    profile_tab = driver.find_element(By.ID, "com.example.agriguard:id/nav_profile")
    profile_tab.click()
    # Verify Profile details like edit profile button
    btn_edit_profile = wait_for_element(driver, (By.ID, "com.example.agriguard:id/btnEditProfile"))
    assert btn_edit_profile is not None

def test_back_button_behavior_on_home(driver):
    """
    Verify clicking the system back button on Home fragment prompts to exit or exits application.
    """
    login_helper(driver)
    
    # Go to Home tab (should already be there)
    home_tab = driver.find_element(By.ID, "com.example.agriguard:id/nav_home")
    home_tab.click()
    
    # Press android back button
    driver.press_keycode(4) # Android back key code
    
    # Verify splash/launcher or check if app goes to background
    # Since it exits, app state should be backgrounded/closed
    assert driver.query_app_state("com.example.agriguard") == 1 or driver.query_app_state("com.example.agriguard") == 2
