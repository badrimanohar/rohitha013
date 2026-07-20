# AgriGuard Web Portal — Page Object Model & Selenium E2E Suite (Phase 4)

This directory contains the prepared Selenium Page Object Model (POM) and E2E automation tests designed for the upcoming web companion portal of **AgriGuard**.

## Architecture & Structure
- `src/main/java/com/example/agriguard/web/pages/BasePage.java` — Core POM base containing explicit waits, element locate helpers, and interaction utilities.
- `src/main/java/com/example/agriguard/web/pages/LoginPage.java` — Login & Google OAuth sign-in encapsulation.
- `src/main/java/com/example/agriguard/web/pages/DashboardPage.java` — Main navigation hub and profile menu actions.
- `src/main/java/com/example/agriguard/web/pages/DiseaseDetectionPage.java` — Crop image upload, AI analysis invocation, confidence verification, and treatment recommendation check.
- `src/test/java/com/example/agriguard/web/tests/AgriGuardWebE2EAutomationTest.java` — Automated workflow verification.

## Future Execution
Once the web portal is deployed, run the automation suite via:
```bash
mvn clean test -Dtest=AgriGuardWebE2EAutomationTest
```
