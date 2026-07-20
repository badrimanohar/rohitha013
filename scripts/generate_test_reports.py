import os
import glob
import xml.etree.ElementTree as ET
import csv
import json
from datetime import datetime

def parse_junit_xml(results_dir):
    test_cases = []
    total_tests = 0
    passed_tests = 0
    failed_tests = 0
    skipped_tests = 0
    total_time = 0.0

    xml_files = glob.glob(os.path.join(results_dir, "TEST-*.xml"))
    if not xml_files:
        # Search recursively
        xml_files = glob.glob(os.path.join(results_dir, "**", "TEST-*.xml"), recursive=True)

    for xml_file in xml_files:
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()
            
            for testcase in root.iter("testcase"):
                total_tests += 1
                name = testcase.get("name", "Unknown Test")
                classname = testcase.get("classname", "Unknown Class")
                time_val = float(testcase.get("time", "0.0"))
                total_time += time_val

                status = "PASS"
                failure_msg = ""
                
                failure = testcase.find("failure")
                if failure is not None:
                    status = "FAIL"
                    failed_tests += 1
                    failure_msg = failure.get("message", "") or failure.text or "Assertion failed"
                else:
                    error = testcase.find("error")
                    if error is not None:
                        status = "ERROR"
                        failed_tests += 1
                        failure_msg = error.get("message", "") or error.text or "Runtime error"
                    else:
                        skipped = testcase.find("skipped")
                        if skipped is not None:
                            status = "SKIPPED"
                            skipped_tests += 1
                            failure_msg = "Test skipped"
                        else:
                            passed_tests += 1

                test_cases.append({
                    "name": name,
                    "classname": classname,
                    "status": status,
                    "time": round(time_val, 3),
                    "message": failure_msg.strip()
                })
        except Exception as e:
            print(f"Error parsing {xml_file}: {e}")

    return {
        "summary": {
            "total": total_tests,
            "passed": passed_tests,
            "failed": failed_tests,
            "skipped": skipped_tests,
            "pass_rate": round((passed_tests / total_tests * 100) if total_tests > 0 else 0.0, 2),
            "duration": round(total_time, 2),
            "timestamp": datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC")
        },
        "tests": test_cases
    }

def generate_html_report(data, output_path):
    summary = data["summary"]
    rows = ""
    for test in data["tests"]:
        badge_color = "#2e7d32" if test["status"] == "PASS" else ("#c62828" if test["status"] in ["FAIL", "ERROR"] else "#f57f17")
        rows += f"""
        <tr>
            <td>{test['classname']}</td>
            <td><strong>{test['name']}</strong></td>
            <td><span style="background:{badge_color}; color:white; padding:4px 8px; border-radius:4px; font-weight:bold;">{test['status']}</span></td>
            <td>{test['time']} s</td>
            <td><code>{test['message'] if test['message'] else 'OK'}</code></td>
        </tr>
        """

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>AgriGuard Automated Test Execution Report</title>
    <style>
        body {{ font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; margin: 30px; color: #333; }}
        .header {{ background: #1b5e20; color: white; padding: 25px; border-radius: 8px; margin-bottom: 25px; }}
        .header h1 {{ margin: 0; font-size: 24px; }}
        .summary-grid {{ display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px; margin-bottom: 30px; }}
        .card {{ background: white; padding: 15px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); text-align: center; }}
        .card h4 {{ margin: 0; color: #666; font-size: 13px; }}
        .card .val {{ font-size: 24px; font-weight: bold; color: #1b5e20; margin-top: 8px; }}
        table {{ width: 100%; border-collapse: collapse; background: white; border-radius: 6px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }}
        th, td {{ padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }}
        th {{ background: #e8f5e9; color: #1b5e20; font-weight: bold; }}
        tr:hover {{ background: #f1f8e9; }}
        code {{ color: #d32f2f; font-size: 13px; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>🌱 AgriGuard Test Execution Dashboard</h1>
        <p>Comprehensive verification report across Unit, Integration, and API test suites.</p>
    </div>
    <div class="summary-grid">
        <div class="card"><h4>Total Tests</h4><div class="val">{summary['total']}</div></div>
        <div class="card"><h4>Passed</h4><div class="val" style="color:#2e7d32;">{summary['passed']}</div></div>
        <div class="card"><h4>Failed</h4><div class="val" style="color:#c62828;">{summary['failed']}</div></div>
        <div class="card"><h4>Pass Rate</h4><div class="val">{summary['pass_rate']}%</div></div>
        <div class="card"><h4>Duration</h4><div class="val">{summary['duration']}s</div></div>
    </div>
    <table>
        <thead>
            <tr>
                <th>Test Class</th>
                <th>Test Case</th>
                <th>Status</th>
                <th>Execution Time</th>
                <th>Execution Logs / Details</th>
            </tr>
        </thead>
        <tbody>
            {rows}
        </tbody>
    </table>
</body>
</html>"""

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(html)

def generate_csv_report(data, output_path):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["Class Name", "Test Name", "Status", "Execution Time (s)", "Message/Failure Details"])
        for test in data["tests"]:
            writer.writerow([test["classname"], test["name"], test["status"], test["time"], test["message"]])

def generate_markdown_summary(data, output_path):
    summary = data["summary"]
    status_emoji = "✅ PASS" if summary["failed"] == 0 and summary["total"] > 0 else "❌ FAIL"
    
    md = f"""# 🌱 AgriGuard CI/CD Test Summary & Final Dashboard

### Execution Status: {status_emoji}

| Metric | Result |
| :--- | :--- |
| **Total Tests Executed** | `{summary['total']}` |
| **Passed Tests** | `{summary['passed']}` ✅ |
| **Failed Tests** | `{summary['failed']}` ❌ |
| **Skipped Tests** | `{summary['skipped']}` ⚠️ |
| **Overall Pass Rate** | `{summary['pass_rate']}%` |
| **Total Execution Duration** | `{summary['duration']} seconds` |
| **Report Generated** | `{summary['timestamp']}` |

---

### Detailed Test Breakdown Table

| Test Class | Test Case | Status | Duration | Notes |
| :--- | :--- | :---: | :---: | :--- |
"""
    for test in data["tests"]:
        badge = "✅ PASS" if test["status"] == "PASS" else ("❌ FAIL" if test["status"] in ["FAIL", "ERROR"] else "⚠️ SKIP")
        md += f"| `{test['classname']}` | **{test['name']}** | {badge} | `{test['time']}s` | {test['message'] if test['message'] else 'OK'} |\n"

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(md)

if __name__ == "__main__":
    results_dir = "app/build/test-results/testDebugUnitTest"
    if not os.path.exists(results_dir):
        print(f"Warning: {results_dir} not found. Checking root build/test-results...")
        results_dir = "app/build/test-results"
    
    data = parse_junit_xml(results_dir)
    print(f"Parsed {data['summary']['total']} test cases. Passed: {data['summary']['passed']}, Failed: {data['summary']['failed']}")
    
    generate_html_report(data, "reports/test_execution_report.html")
    generate_csv_report(data, "reports/test_execution_report.csv")
    generate_markdown_summary(data, "reports/test_execution_summary.md")
    print("Reports generated successfully inside reports/ directory.")
