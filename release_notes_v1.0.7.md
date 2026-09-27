## 🌟 What's New & Enhancements
- **Full Data Backup & Export (JSON)**: Users can now create a comprehensive, portable JSON backup file directly from the Settings sheet under `DATA MANAGEMENT`. The backup encapsulates:
  - All Tasks (including titles, notes, checklists/subtasks, priority, tags, energy level, estimated minutes, countdown status, and recurrence).
  - All Timetable Sessions with exact weekly day (`MON`, `TUE`, `WED`), time windows, locations (`K301`, `K302`, `P1 CLINIC`), and dual alarm configurations (`alarmOnStart`, `alarmOnFinish`).
  - All Habits (with current & best streaks, target-per-day, and completed calendar dates).
  - All Routines (steps, day-parts, icons, and completion states).
  - All Projects & Sections (colors, icons, favorite states).
  - All Goals & OKRs (current progress, target values, units).
  - All Custom Task Templates (checklists, recurrence presets, priority).
  - Core User Profile & Preferences (display name, gender, theme mode, country, timezone, sound/haptic toggles, banner configurations).
- **Comprehensive Data Restoration & Import (JSON)**: Added a frictionless file picker utilizing Android's Storage Access Framework (`OpenDocument`) to import backups from internal storage, SD cards, or cloud providers (Google Drive, Nextcloud, etc.).
- **Automated Timetable Alarm Re-Arming**: When importing a backup file, any scheduled timetable lectures or clinics with active start or finish alarms are automatically registered with Android `AlarmManager` for weekly recurrent alerting.

## 🎨 UI/UX Polish & Visual Design
- **Apple Liquid Glass Settings Integration**: Designed dedicated Export and Import action rows within the `DATA MANAGEMENT` section of `LunaSettingsSheet`, styled with high-blur glass surfaces, crisp typography, and responsive touch highlights.
- **Untitled UI Iconography**: Handcrafted minimalist 24x24 vector icons for `UntitledIcons.Download` and `UntitledIcons.Upload` matching the design language.
- **Real-Time Restoration Feedback**: Added an interactive status pill displaying live confirmation messages and exact restoration counts (e.g. `✓ Restored: 13 tasks, 4 habits, 3 routines, 2 projects, 2 goals, 3 templates`), with instant dismiss controls.

## 🐛 Bug Fixes & Refinements
- **Deduplication on Import**: Intelligent entity matching avoids duplicate entries when importing backups over existing databases; existing tasks matching title, day, and time are gracefully updated without duplicate entries.
- **Foreign Key Integrity**: Subtasks are dynamically re-mapped to newly generated parent task IDs to prevent orphaned checklist items.
- **Null-Safety & Mime Flexibility**: Added broad mime-type support (`application/json`, `text/*`, `*/*`) to accommodate OEM file managers that categorize `.json` files as generic text or octet streams.

## ⚙️ Technical, Performance & Architecture
- **Dedicated Backup Engine**: Created `LunaBackupManager` in `com.luna.app.data.backup` utilizing native Android JSON serialization for high performance without third-party library overhead.
- **Batch Repository Operations**: Added batch import primitives to `TaskRepository` (`importTaskWithSubtasks`, `importHabit`, `importRoutine`, `importProject`, `importGoal`) for atomic operations.
- **Strict AGENTS.md Conformance**:
  - Incremented `versionName` to `1.0.7` and `versionCode` to `107` in `app/build.gradle.kts`.
  - Dynamic attribution reflection in `BuildConfig.VERSION_NAME` via `LunaAttribution.APP_VERSION`.
  - Single-ABI signed release build restricted to `arm64-v8a`.

## 📦 Release Assets
- **Application Binary**: `Luna-v1.0.7-arm64-release.apk`
- **Target Architecture**: `arm64-v8a`
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35) / Compile SDK 36
