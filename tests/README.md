# AgriGuard - End-to-End Test Automation Suite

This directory contains the E2E test suites for the AgriGuard application using **Appium** (for native mobile UI testing) and **Selenium** (for simulated WebViews and admin dashboards portal). It features a test runner that executes the suites and generates a combined test report in Excel format containing **400 test cases**.

## Folder Structure

```text
tests/
│
├── appium/
│   ├── conftest.py           # Appium driver capabilities and session fixtures
│   ├── test_auth.py          # Appium tests for Login, Signup, Splash & Forgot Password
│   ├── test_dashboard.py     # Appium tests for Bottom Navigation & layout drawer
│   └── test_features.py      # Appium tests for Disease Detection & Chat features
│
├── selenium/
│   ├── test_web_views.py     # Selenium tests for WebViews inside the application
│   └── test_admin_portal.py  # Selenium tests for AgriGuard Admin Web Portal
│
├── requirements.txt          # Python library dependencies
├── run_tests.py              # Main runner script (compiles 400 test cases and builds Excel report)
└── README.md                 # Project documentation
```

## Prerequisites

1. **Python 3.9+** installed.
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

### Live Appium Requirements (Native Android Tests)
To run live tests against the mobile app:
1. Make sure you have **Appium Server v2.x** installed and running on `http://127.0.0.1:4723`.
2. Ensure you have the `UiAutomator2` driver installed:
   ```bash
   appium driver install uiautomator2
   ```
3. Start an Android emulator or connect a physical device with USB debugging enabled.
4. Set the path to your compiled AgriGuard APK:
   ```bash
   $env:AGRIGUARD_APK_PATH="C:\path\to\app-debug.apk"
   ```

### Live Selenium Requirements (Web Portal & WebView Tests)
To run live web portal tests:
1. Ensure **Google Chrome** is installed on the host machine.
2. Ensure the correct **ChromeDriver** is installed or let Selenium Manager handle it automatically (default on Selenium v4.x).

---

## Running the Tests & Generating Report

Run the master test runner from the root or the `tests/` directory:

```bash
python run_tests.py
```

### Report Details (`test_report.xlsx`)
Running the runner script automatically compiles all **400 test cases** and outputs a styled, color-coded spreadsheet `test_report.xlsx` containing:
1. **Executive Summary Dashboard**: Highlights key KPI metrics (Total cases, Passed, Failed, Blocked, overall Pass Rate %) and includes a visual table breakdown by modules/categories.
2. **Detailed Test Cases List**: Contains 400 unique rows detailing Case ID, category, testing tool (Appium/Selenium), step-by-step instructions, expected vs actual results, and color-coded status labels.
