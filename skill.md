# Android System Skill: Root-Enabled ADB Toolkit

This guide provides a comprehensive set of ADB commands for system discovery, storage management, app control, and hardware monitoring on Android devices. It is designed for AI agents operating with elevated privileges to perform deep system analysis and automation.

---

## 1. System Discovery
Quickly assess the device state and environment.

*   **List Installed Packages:**
    ```bash
    adb shell pm list packages -f # Includes path to APK
    adb shell pm list packages -3 # Only 3rd party apps
    ```
*   **Battery Status & Health:**
    ```bash
    adb shell dumpsys battery
    ```
*   **Retrieve System Properties:**
    ```bash
    adb shell getprop | grep "ro.build.version" # OS version info
    adb shell getprop | grep "ro.product.model" # Device model
    ```
*   **Window & Display Metrics:**
    ```bash
    adb shell wm size
    adb shell wm density
    ```

---

## 2. Storage Management (Root Access Required)
Analyze disk usage and locate specific file types across the filesystem.

*   **Filesystem Disk Usage:**
    ```bash
    adb shell df -h
    ```
*   **Locate Large Files (>100MB):**
    ```bash
    adb shell su -c "find /data -type f -size +100M -exec ls -lh {} \;"
    ```
*   **Identify Hidden Directories:**
    ```bash
    adb shell su -c "find /sdcard -name '.*' -type d"
    ```
*   **Check Directory Size (Recursive):**
    ```bash
    adb shell su -c "du -sh /data/data/com.example.app"
    ```

---

## 3. App Control & Permissions
Manage application lifecycles and security context.

*   **Start/Force-Stop Activities:**
    ```bash
    adb shell am start -n com.package.name/.MainActivity
    adb shell am force-stop com.package.name
    ```
*   **Reset App Data:**
    ```bash
    adb shell pm clear com.package.name
    ```
*   **Permission Management:**
    ```bash
    adb shell pm grant com.package.name android.permission.READ_EXTERNAL_STORAGE
    adb shell pm revoke com.package.name android.permission.CAMERA
    ```
*   **Dump App Details:**
    ```bash
    adb shell dumpsys package com.package.name
    ```

---

## 4. Media Interaction
Root-based scanning and indexing logic for media assets.

*   **Find Media in DCIM:**
    ```bash
    adb shell su -c "find /sdcard/DCIM -type f \( -iname '*.jpg' -o -iname '*.png' -o -iname '*.mp4' \)"
    ```
*   **Trigger Media Scanner for a Specific File:**
    ```bash
    adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/DCIM/Camera/IMG_001.jpg
    ```
*   **List Media Content Provider Rows:**
    ```bash
    adb shell content query --uri content://media/external/images/media --projection _display_name:mime_type
    ```

---

## 5. AI Hardware & NPU Monitoring
Check the status of AI-specific components and silicon usage.

*   **AICore Service Status:**
    ```bash
    adb shell dumpsys aicore
    adb shell pm list packages | grep "aicore"
    ```
*   **GPU/NPU Utilization (Platform Dependent):**
    *   *Qualcomm:* `adb shell "cat /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"`
    *   *General:* `adb shell su -c "cat /proc/interrupts | grep -i 'npu\|tpu'"`
*   **Neural Networks API (NNAPI) Drivers:**
    ```bash
    adb shell su -c "ls /vendor/lib64/ | grep -i 'nnapi\|hw'"
    ```

---

## 6. Logcat Monitoring
Filter system logs for project-specific tags and high-priority events.

*   **Consolidated Filter:**
    ```bash
    adb logcat -s AICore:V MultimodalIndexerClient:D WorkManager:I *:W
    ```
*   **Capture Logs to File with Timestamps:**
    ```bash
    adb logcat -v threadtime > system_dump.log
    ```
*   **Search Logs for Specific Pattern:**
    ```bash
    adb logcat -d | grep -i "exception\|crash"
    ```

---
**Note:** Use `adb shell su -c "[command]"` when interacting with restricted paths like `/data` or `/vendor`. Always ensure the device has `root` access or `adb root` is enabled in developer settings.
