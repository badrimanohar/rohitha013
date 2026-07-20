# AgriGuard — k6 Load Testing Suite (Phase 5)

This suite performs performance, concurrency, and stress testing against AgriGuard's advisory and detection backend APIs using **k6**.

## Test Configuration
- **Peak Concurrency:** 100 Virtual Users (VUs)
- **Ramp-up / Sustained / Ramp-down:** 30s -> 60s (100 VUs) -> 30s
- **Thresholds:**
  - `http_req_duration`: p(95) < 800ms, avg < 300ms
  - `http_req_failed`: rate < 2%

## Running Locally
Install [k6](https://k6.io/docs/get-started/installation/) and run:
```bash
k6 run load-tests/agriguard_load_test.js
```
Upon completion, two report artifacts are automatically generated in the working directory:
- `load-test-summary.html` (Visual HTML dashboard)
- `load-test-summary.json` (Structured JSON metrics)
