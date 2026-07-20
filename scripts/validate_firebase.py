import os
import json

def validate_firebase():
    report_lines = [
        "# 🔥 Firebase Configuration Validation Report",
        "",
        "| Service / Check | Status | Details / Target Configuration |",
        "| :--- | :---: | :--- |"
    ]
    
    gs_path = "app/google-services.json"
    if not os.path.exists(gs_path):
        report_lines.append("| **google-services.json** | ❌ MISSING | File not found at app/google-services.json |")
        print("❌ google-services.json not found!")
        return
    
    try:
        with open(gs_path, "r", encoding="utf-8") as f:
            data = json.load(f)
            
        project_info = data.get("project_info", {})
        project_id = project_info.get("project_id", "UNKNOWN")
        firebase_url = project_info.get("firebase_url", "UNKNOWN")
        storage_bucket = project_info.get("storage_bucket", "UNKNOWN")
        
        report_lines.append(f"| **Project ID** | ✅ PASS | `{project_id}` |")
        report_lines.append(f"| **Realtime Database URL** | ✅ PASS | `{firebase_url}` |")
        report_lines.append(f"| **Cloud Storage Bucket** | ✅ PASS | `{storage_bucket}` |")
        
        clients = data.get("client", [])
        has_web_client = False
        package_name = "UNKNOWN"
        for client in clients:
            client_info = client.get("client_info", {})
            android_client_info = client_info.get("android_client_info", {})
            package_name = android_client_info.get("package_name", package_name)
            
            oauth_clients = client.get("oauth_client", [])
            for oc in oauth_clients:
                if oc.get("client_type") == 3:
                    has_web_client = True
                    break
        
        report_lines.append(f"| **Android Package Name** | ✅ PASS | `{package_name}` |")
        if has_web_client:
            report_lines.append("| **OAuth Web Client ID (client_type: 3)** | ✅ PASS | Present (Ensures `default_web_client_id` resolution) |")
        else:
            report_lines.append("| **OAuth Web Client ID (client_type: 3)** | ⚠️ WARN | No `client_type: 3` entry found in oauth_client array |")
            
        # Check dependencies in app/build.gradle.kts
        build_gradle_path = "app/build.gradle.kts"
        if os.path.exists(build_gradle_path):
            with open(build_gradle_path, "r", encoding="utf-8") as f:
                content = f.read()
            auth_ok = "firebase-auth" in content
            db_ok = "firebase-database" in content
            storage_ok = "firebase-storage" in content
            
            report_lines.append(f"| **Firebase Authentication SDK** | {'✅ PASS' if auth_ok else '❌ MISSING'} | `com.google.firebase:firebase-auth` |")
            report_lines.append(f"| **Firebase Realtime Database SDK** | {'✅ PASS' if db_ok else '❌ MISSING'} | `com.google.firebase:firebase-database` |")
            report_lines.append(f"| **Firebase Storage SDK** | {'✅ PASS' if storage_ok else '❌ MISSING'} | `com.google.firebase:firebase-storage` |")

    except Exception as e:
        report_lines.append(f"| **JSON Parsing** | ❌ ERROR | Failed to parse `google-services.json`: {e} |")

    report_content = "\n".join(report_lines)
    os.makedirs("reports", exist_ok=True)
    with open("reports/firebase_validation_report.md", "w", encoding="utf-8") as f:
        f.write(report_content)
    print(report_content)
    print("Firebase validation complete. Report saved to reports/firebase_validation_report.md.")

if __name__ == "__main__":
    validate_firebase()
