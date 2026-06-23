import pytest
import os
from appium import webdriver
from appium.options.android import UiAutomator2Options

# Default AgriGuard Android capabilities
DEFAULT_CAPS = {
    "platformName": "Android",
    "automationName": "UiAutomator2",
    "deviceName": "Android_Emulator",
    "appPackage": "com.example.agriguard",
    "appActivity": "com.example.agriguard.activities.SplashActivity",
    "noReset": False,
    "fullReset": False,
    "newCommandTimeout": 300,
}

@pytest.fixture(scope="session")
def appium_server_url():
    """Returns the URL of the running Appium server."""
    return os.environ.get("APPIUM_SERVER_URL", "http://127.0.0.1:4723")

@pytest.fixture(scope="function")
def driver(appium_server_url):
    """
    Setup and teardown fixture for the Appium WebDriver.
    Will attempt connection to Appium server, else fail gracefully for non-live runs.
    """
    options = UiAutomator2Options()
    options.load_capabilities(DEFAULT_CAPS)
    
    # Allow overriding app path via environment variable
    app_path = os.environ.get("AGRIGUARD_APK_PATH")
    if app_path:
        options.app = app_path

    driver = None
    try:
        driver = webdriver.Remote(appium_server_url, options=options)
        yield driver
    except Exception as e:
        pytest.fail(f"Could not connect to Appium Server at {appium_server_url}. Make sure Appium is running. Error: {e}")
    finally:
        if driver:
            driver.quit()
