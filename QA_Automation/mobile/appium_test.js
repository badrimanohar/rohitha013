const { remote } = require('webdriverio');
const fs = require('fs');
const path = require('path');

const outputDir = path.join(__dirname, 'output');
if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir);

const capabilities = {
    platformName: 'Android',
    'appium:automationName': 'UiAutomator2',
    'appium:deviceName': 'Android_Device', // Replace with your device name
    'appium:app': path.join(__dirname, '../../app/build/outputs/apk/debug/app-debug.apk'),
    'appium:appPackage': 'com.example.agrinova',
    'appium:appActivity': '.SplashActivity',
    'appium:noReset': true
};

const wdOpts = {
    hostname: process.env.APPIUM_HOST || 'localhost',
    port: parseInt(process.env.APPIUM_PORT, 10) || 4723,
    logLevel: 'info',
    capabilities,
};

async function runMobileE2E() {
    const driver = await remote(wdOpts);
    const logFile = path.join(outputDir, 'appium_execution_log.txt');

    const log = (msg) => {
        const entry = `[${new Date().toISOString()}] ${msg}\n`;
        fs.appendFileSync(logFile, entry);
        console.log(msg);
    };

    try {
        log("Mobile E2E Test Started");

        // 1. Wait for Login Screen
        const emailField = await driver.$('id:com.example.agrinova:id/et_email');
        await emailField.waitForDisplayed({ timeout: 15000 });
        log("App Launched: Login Screen visible");

        // 2. Perform Login
        await emailField.setValue('farmer@agrinova.com');
        await driver.$('id:com.example.agrinova:id/et_password').setValue('Password123');
        await driver.$('id:com.example.agrinova:id/btn_login').click();
        log("Login button clicked");

        // 3. Verify Dashboard
        const welcomeText = await driver.$('id:com.example.agrinova:id/tv_welcome');
        await welcomeText.waitForDisplayed({ timeout: 10000 });
        log("Dashboard Loaded: User welcomed");
        await driver.saveScreenshot(path.join(outputDir, 'dashboard_success.png'));

        // 4. Test Disease Detection Navigation
        await driver.$('id:com.example.agrinova:id/card_ai_doctor').click();
        log("Navigating to AI Disease Detection");

        const detectBtn = await driver.$('id:com.example.agrinova:id/btn_detect');
        await detectBtn.waitForDisplayed({ timeout: 5000 });
        log("Disease Detection Screen reached");
        await driver.saveScreenshot(path.join(outputDir, 'detection_screen.png'));

        // 5. Profile Settings Check
        await driver.back();
        await driver.$('id:com.example.agrinova:id/action_profile').click();
        log("Checking Profile Settings");

        const nameField = await driver.$('id:com.example.agrinova:id/et_name');
        await nameField.waitForDisplayed({ timeout: 5000 });
        const currentName = await nameField.getText();
        log(`Current Profile Name: ${currentName}`);

        log("End-to-End Mobile Test Completed successfully");

    } catch (error) {
        log(`ERROR: ${error.message}`);
        await driver.saveScreenshot(path.join(outputDir, 'error_state.png'));
    } finally {
        await driver.deleteSession();
        log("Appium Session Closed");
    }
}

runMobileE2E();
