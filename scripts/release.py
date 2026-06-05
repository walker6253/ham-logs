#!/usr/bin/env python3
"""一键构建 Release APK：versionCode+1, versionName patch+1, 构建, 重命名"""
import re, subprocess, os, sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
gradle_path = os.path.join(root, "app", "build.gradle.kts")

with open(gradle_path, "r", encoding="utf-8") as f:
    content = f.read()

vc_match = re.search(r"versionCode\s*=\s*(\d+)", content)
vn_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)

old_vc = int(vc_match.group(1))
old_vn = vn_match.group(1)
parts = old_vn.split(".")
new_vn = f"{parts[0]}.{parts[1]}.{int(parts[2])+1}"
new_vc = old_vc + 1

print(f"版本递增: versionCode {old_vc} -> {new_vc}, versionName {old_vn} -> {new_vn}")

content = re.sub(r"versionCode\s*=\s*\d+", f"versionCode = {new_vc}", content)
content = re.sub(r'versionName\s*=\s*"[^"]+"', f'versionName = "{new_vn}"', content)

with open(gradle_path, "w", encoding="utf-8") as f:
    f.write(content)

app_id_match = re.search(r'applicationId\s*=\s*"([^"]+)"', content)
proj_name = app_id_match.group(1).split(".")[-1] if app_id_match else "app"

print("开始构建...")
result = subprocess.run(
    [os.path.join(root, "gradlew.bat"), "assembleRelease"],
    cwd=root, capture_output=True, text=True
)
if result.returncode != 0:
    print("构建失败!")
    print(result.stderr[-2000:])
    sys.exit(1)

apk_dir = os.path.join(root, "app", "build", "outputs", "apk", "release")
old_path = os.path.join(apk_dir, "app-release.apk")
new_path = os.path.join(apk_dir, f"{proj_name}-{new_vn}.apk")
if os.path.exists(new_path):
    os.remove(new_path)
os.rename(old_path, new_path)

size_mb = os.path.getsize(new_path) / 1024 / 1024
print(f"完成: {proj_name}-{new_vn}.apk ({size_mb:.2f} MB)")
