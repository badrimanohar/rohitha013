import pytest
import os
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def wait_for_element(driver, locator, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located(locator)
    )

def login_and_navigate_to_tab(driver, tab_id):
    """Helper to log in and switch to a bottom nav tab."""
    # Input credentials
    email_field = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etEmail"))
    password_field = driver.find_element(By.ID, "com.example.agriguard:id/etPassword")
    login_btn = driver.find_element(By.ID, "com.example.agriguard:id/btnLogin")
    
    email_field.send_keys("farmer@agriguard.com")
    password_field.send_keys("Password123!")
    login_btn.click()
    
    # Click target tab
    tab_elem = wait_for_element(driver, (By.ID, tab_id))
    tab_elem.click()

def test_disease_detection_gallery_upload(driver):
    """
    Verify upload from gallery and crop detection triggers.
    """
    login_and_navigate_to_tab(driver, "com.example.agriguard:id/nav_detect")
    
    # Click gallery button
    btn_gallery = wait_for_element(driver, (By.ID, "com.example.agriguard:id/btnGallery"))
    btn_gallery.click()
    
    # Simulating file selector on android (System Gallery app)
    # Standard android locator for first photo in list
    first_photo = wait_for_element(driver, (By.XPATH, "//android.widget.ImageView[1]"), timeout=15)
    first_photo.click()
    
    # Preview should become visible
    btn_detect = wait_for_element(driver, (By.ID, "com.example.agriguard:id/btnDetect"))
    assert btn_detect.is_displayed()
    
    # Click detect button
    btn_detect.click()
    
    # Wait for loading indicator to disappear and resultCard to load
    result_card = wait_for_element(driver, (By.ID, "com.example.agriguard:id/resultCard"), timeout=30)
    assert result_card.is_displayed()
    
    # Assert result details
    crop_name = driver.find_element(By.ID, "com.example.agriguard:id/tvResultCrop").text
    disease_name = driver.find_element(By.ID, "com.example.agriguard:id/tvResultDisease").text
    assert "Crop:" in crop_name
    assert len(disease_name) > 0

def test_community_chat_send_message(driver):
    """
    Verify posting a text message in Community Chat.
    """
    login_and_navigate_to_tab(driver, "com.example.agriguard:id/nav_community")
    
    # Enter chat activity by clicking on first community chat item
    first_chat_item = wait_for_element(driver, (By.XPATH, "//android.widget.TextView[@text='General Chat']"))
    first_chat_item.click()
    
    # Enter message text
    message_input = wait_for_element(driver, (By.ID, "com.example.agriguard:id/etChatMessage"))
    message_input.send_keys("Hello, this is an automated Appium test message.")
    
    # Send message
    send_btn = driver.find_element(By.ID, "com.example.agriguard:id/btnSendMessage")
    send_btn.click()
    
    # Verify the message displays in the message list
    sent_msg = wait_for_element(driver, (By.XPATH, "//android.widget.TextView[@text='Hello, this is an automated Appium test message.']"))
    assert sent_msg is not None

def test_market_prices_search(driver):
    """
    Verify filtering crop items by name in Market Prices.
    """
    login_and_navigate_to_tab(driver, "com.example.agriguard:id/nav_prices")
    
    # Select search bar
    search_bar = wait_for_element(driver, (By.ID, "com.example.agriguard:id/svCropSearch"))
    search_bar.click()
    
    # Type 'Tomato'
    search_input = driver.find_element(By.XPATH, "//android.widget.EditText")
    search_input.send_keys("Tomato")
    
    # Verify only Tomato entries remain in the list
    first_row_title = wait_for_element(driver, (By.ID, "com.example.agriguard:id/tvCropItemName"))
    assert "Tomato" in first_row_title.text
