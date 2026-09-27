# 🌙 Luna v1.0.6: Weekly Dentistry Timetable, Dual Alarms & Chronological Engine

Welcome to **Luna v1.0.6**! This major update brings a university timetable system built natively into Luna, tailored for Dentistry (Level 4, Fall 2026-2027 · Week 7), complete visual distinction between Lectures and Clinics, dual start & finish exact alarms with weekly recurrence, complete automated deduplication, and chronological task sorting across all smart filters.

---

## 🌟 What's New & Enhancements
* **Native Weekly Timetable View (`AppViewMode.TIMETABLE`)**:
  - Integrated directly into the top horizontal view selector alongside List, Calendar, Habits, Routines, and Matrix.
  - Dedicated view tab (`🦷 Timetable`) with immediate access to weekly academic schedules.
  - Header statistics covering all 13 sessions, 20 weekly hours, 7 lectures, and 6 clinics.
  - Interactive day filter tabs: **All Days**, **Monday** (4 sessions · 6h), **Tuesday** (4 sessions · 6h), and **Wednesday** (5 sessions · 8h).
  - Expandable **Course Directory & Departments** legend covering all 7 dentistry courses, departments, and credit hours.
* **Dual Alarms (Start & Finish) + Weekly Repetition**:
  - Every timetable session features both a **Start Alarm** (alerting when class begins with room location and cohort group) and a **Finish Alarm** (alerting when the lecture/clinic ends).
  - Automatic weekly recurrence re-arming: alarms automatically re-arm for the next week (+7 days) when triggered.
  - Instant testing toolbar actions: `🔔 Test Start Alarm (10s)` and `🏁 Test End Alarm (10s)`.
* **Automatic Startup Seeding**:
  - `DentalTimetableSeeder` is invoked automatically during application launch in `LunaApplication.kt`, ensuring timetable sessions are always present without manual imports.

---

## 🎨 UI/UX Polish & Visual Design
* **Distinct Visual Identity for Lectures vs. Clinics**:
  - **📚 Lecture Blocks** (`OPD 511`, `PDD 425`, `OMR 531`, `CDD 415`, `CDD 424`, `OMS 421`, `OMS 411`):
    - Solid continuous luminous glass border with 5dp solid vertical stripe.
    - Soft badge with book icon (`📚 LECTURE`), room details (`📍 K301` / `📍 K302`), and `Full cohort` indicator.
  - **🦷 Clinic Blocks** (`PDD 425`, `OPD 511`, `CDD 415`, `CDD 424`, `OMS 411`, `OMR 531`):
    - **Custom Canvas Dashed Border (`Modifier.dashedBorder`)** with 1.8dp dashed stroke.
    - Clinical striped accent gradient and glowing badge with tooth icon (`🦷 CLINIC · <Group>`).
    - Dedicated clinical room assignments (`📍 M106`, `📍 M116`, `📍 M117`, `📍 M120`, `📍 M220`, `📍 K111 / M104`).
    - Prominent `⚠️ P1 CLINIC` urgent attendance tag.
* **Top Header Streamlining**:
  - Removed the redundant top header settings button from `LunaHeader.kt`.
  - Retained the floating liquid glass settings orb FAB in `HomeScreen.kt` for settings access.
* **Interactive Session Detail Bottom Sheet**:
  - Tapping any timetable block opens a bottom sheet with course title, code, time slot, room, group, instructor, alarms status, and quick shortcuts.

---

## 🐛 Bug Fixes & Refinements
* **Complete Database Deduplication Engine**:
  - Added an automated database deduplication purge to eliminate historical duplicate sessions from Room database.
  - Guarantees 0 duplicate entries across all schedule grids and task lists.
* **Chronological Task Sorting Across All Tabs**:
  - Replaced index-based ordering with a universal `chronologicalComparator` in `TaskViewModel.kt`.
  - All smart filter tabs (`Today`, `Tomorrow`, `This Week`, `Overdue`, `High Priority`, `Deep Work`, `Quick Wins`, `Pinned`, `All`, and `Completed`) are strictly ordered chronologically by scheduled date and time.
* **Notification Deep-Linking**:
  - Tapping any session alarm notification directly launches Luna and routes to the `🦷 Timetable` view.

---

## ⚙️ Technical, Performance & Architecture
* **Exact Alarm Scheduling (`LunaAlarmScheduler`)**:
  - Separate request code namespaces for start alarms (`taskId * 10`) and finish alarms (`taskId * 10 + 1`) to eliminate PendingIntent collisions.
  - Compatibility with Android 14+ `SCHEDULE_EXACT_ALARM` permissions.
* **Dynamic Attribution & Version Derivation**:
  - Build version dynamically sourced from `BuildConfig.VERSION_NAME` via `LunaAttribution.APP_VERSION`.
* **Zero Compilation Warnings & Proguard Guardrails**:
  - Clean Kotlin 2.0 / Compose compilation without unresolved token errors.

---

## 📦 Release Assets
* **File Name**: `Luna-v1.0.6-arm64-release.apk`
* **Target Architecture**: `arm64-v8a`
* **Minimum SDK**: Android 8.0 (API level 26)
* **Target SDK**: Android 15 (API level 35)
* **Compiled SDK**: API level 36
* **Size**: 14.43 MB
* **Signing**: Release Signed (Keystore verified)
