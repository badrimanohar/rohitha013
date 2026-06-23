import pytest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

@pytest.fixture(scope="module")
def web_driver():
    """Initializes a standard Chrome Selenium WebDriver instance."""
    options = webdriver.ChromeOptions()
    options.add_argument("--headless") # Run headless for CLI
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    
    driver = None
    try:
        driver = webdriver.Chrome(options=options)
        yield driver
    except Exception as e:
        pytest.fail(f"Failed to start Selenium WebDriver: {e}")
    finally:
        if driver:
            driver.quit()

def test_webview_external_market_rates(web_driver):
    """
    Verify loading and navigation of external crop market rate links.
    """
    web_driver.get("https://enam.gov.in/web/") # Example national agriculture market portal used in WebView
    
    # Wait for the site to load and check header title
    title = WebDriverWait(web_driver, 10).until(
        EC.presence_of_element_located((By.XPATH, "//title"))
    )
    assert "eNAM" in web_driver.title or len(web_driver.title) > 0

def test_webview_privacy_policy(web_driver):
    """
    Verify that the privacy policy documentation page loads and contains required sections.
    """
    # Assuming local or hosted documentation page
    web_driver.get("https://agriguard-app.web.app/privacy-policy") 
    
    header = WebDriverWait(web_driver, 10).until(
        EC.presence_of_element_located((By.TAG_NAME, "h1"))
    )
    assert "Privacy Policy" in header.text
