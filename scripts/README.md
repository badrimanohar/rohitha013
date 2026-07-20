# AgriGuard — Test & Security Report Generator Utility

This folder contains helper scripts designed to parse automated test, lint, and security scan outputs and produce professional dashboard summaries.

## `generate_test_reports.py`
Automatically scans `app/build/test-results/testDebugUnitTest/` for standard `TEST-*.xml` JUnit files produced during `./gradlew test` and exports three comprehensive artifacts to the `reports/` directory:
1. `test_execution_report.html` — Interactive visual dashboard for QA & DevOps teams.
2. `test_execution_report.csv` — Tabular Excel/CSV export suitable for reporting metrics.
3. `test_execution_summary.md` — GitHub Actions Markdown summary table embedded directly into `$GITHUB_STEP_SUMMARY`.
