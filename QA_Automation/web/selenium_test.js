const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const xlsx = require('xlsx');
const path = require('path');

async function runWebE2E() {
    let driver = await new Builder().forBrowser('chrome').build();
    let testResults = [];

    const logResult = (testId, feature, status, remarks) => {
        testResults.push({
            "Test ID": testId,
            "Feature": feature,
            "Status": status,
            "Execution Time": new Date().toLocaleString(),
            "Remarks": remarks
        });
        console.log(`[${status}] ${feature}: ${remarks}`);
    };

    try {
        // 1. Launch Application
        await driver.get('https://agrinova-app.web.app'); // Replace with actual web URL
        logResult("WEB_01", "Launch App", "PASS", "Application loaded successfully");

        // 2. User Login
        try {
            await driver.findElement(By.id('email')).sendKeys('testuser@agrinova.com');
            await driver.findElement(By.id('password')).sendKeys('Test@123');
            await driver.findElement(By.id('login_btn')).click();

            await driver.wait(until.elementLocated(By.id('dashboard_header')), 10000);
            logResult("WEB_02", "Login", "PASS", "Logged in successfully to Dashboard");
        } catch (e) {
            logResult("WEB_02", "Login", "FAIL", e.message);
        }

        // 3. Navigate to Disease Detection (Web version)
        try {
            await driver.findElement(By.id('nav_disease')).click();
            await driver.wait(until.elementLocated(By.id('upload_section')), 5000);
            logResult("WEB_03", "Navigation", "PASS", "Navigated to Disease Detection section");
        } catch (e) {
            logResult("WEB_03", "Navigation", "FAIL", e.message);
        }

        // 4. Check Community Section
        try {
            await driver.findElement(By.id('nav_community')).click();
            let communityTitle = await driver.findElement(By.className('section-title')).getText();
            if (communityTitle.includes("Community")) {
                logResult("WEB_04", "Community Feature", "PASS", "Community section is visible");
            } else {
                logResult("WEB_04", "Community Feature", "FAIL", "Community title mismatch");
            }
        } catch (e) {
            logResult("WEB_04", "Community Feature", "FAIL", e.message);
        }

    } catch (globalError) {
        logResult("FATAL", "E2E Flow", "ERROR", globalError.message);
    } finally {
        // Generate Excel Report
        const worksheet = xlsx.utils.json_to_sheet(testResults);
        const workbook = xlsx.utils.book_new();
        xlsx.utils.book_append_sheet(workbook, worksheet, "Web_Test_Analysis");

        const reportPath = path.join(__dirname, 'Web_E2E_Report.xlsx');
        xlsx.writeFile(workbook, reportPath);

        console.log(`\nEnd-to-End Web Test Completed. Report generated at: ${reportPath}`);
        await driver.quit();
    }
}

runWebE2E();
