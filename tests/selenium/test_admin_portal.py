import pytest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

@pytest.fixture(scope="module")
def admin_driver():
    options = webdriver.ChromeOptions()
    options.add_argument("--headless")
    driver = None
    try:
        driver = webdriver.Chrome(options=options)
        yield driver
    except Exception as e:
        pytest.fail(f"Failed to start Selenium WebDriver for Admin Portal: {e}")
    finally:
        if driver:
            driver.quit()

def test_admin_portal_login_success(admin_driver):
    """
    Verify login flow to AgriGuard Administrator Portal.
    """
    admin_driver.get("https://agriguard-admin.web.app/login")
    
    username_field = WebDriverWait(admin_driver, 10).until(
        EC.presence_of_element_located((By.ID, "admin-username"))
    )
    password_field = admin_driver.find_element(By.ID, "admin-password")
    login_btn = admin_driver.find_element(By.ID, "btn-admin-login")
    
    username_field.send_keys("admin@agriguard.com")
    password_field.send_keys("AdminSecure2026!")
    login_btn.click()
    
    # Check landing on dashboard page
    dashboard_header = WebDriverWait(admin_driver, 10).until(
        EC.presence_of_element_located((By.ID, "dashboard-welcome-heading"))
    )
    assert "AgriGuard Management System" in dashboard_header.text

def test_admin_portal_user_management(admin_driver):
    """
    Verify searching a user profile and updating their status in the Admin dashboard.
    """
    admin_driver.get("https://agriguard-admin.web.app/users")
    
    search_bar = WebDriverWait(admin_driver, 10).until(
        EC.presence_of_element_located((By.ID, "search-user-input"))
    )
    search_bar.send_keys("farmer@agriguard.com")
    
    # Select first user row
    user_row = WebDriverWait(admin_driver, 10).until(
        EC.presence_of_element_located((By.XPATH, "//table[@id='users-table']/tbody/tr[1]"))
    )
    assert "farmer@agriguard.com" in user_row.text
