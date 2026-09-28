## 🌟 What's New & Enhancements
- **Multi-Tier Native Back Navigation Hierarchy**: Implemented a comprehensive, context-aware `BackHandler` in `HomeScreen`:
  - Closes full-screen global search overlay.
  - Exits multi-select bulk selection mode.
  - Closes active bottom sheets (Settings, Task Creation, Task Detail, Templates).
  - Navigates from secondary task views (`Timetable`, `Calendar`, `Habits`, `Routines`, `Matrix`) back to `List` mode.
  - Navigates from non-Home primary tabs (`Tasks`, `Timers`, `Stats`) back to the `Home` dashboard before exiting the application.
- **Timetable Deep-Link Pager Synchronization**: Resolved the intent routing gap where timetable notifications and external shortcuts selected the timetable mode without switching tabs. `HomeScreen` now automatically synchronizes and animates the `HorizontalPager` directly to the `TASKS` tab upon arrival.
- **Interactive Live Timetable Alarm Controls**: Users can now directly toggle Start and Finish alarms for individual university lectures and clinical sessions via live switches inside the `TimetableSessionDetailSheet`, updating Room persistence and Android `AlarmManager` schedules in real time.
- **Full Pomodoro Cycle Progression**: Enhanced `LunaFocusView` to track completed work intervals and auto-advance into a `Long Break` after 4 work sessions (`Work x4 → Long Break`), alongside standard short breaks.
- **Dynamic Academic Term & Calendar Week**: Replaced static timetable headers with real-time academic term (`Fall/Spring/Summer <Year>`) and calendar week calculations based on device date.
- **Dynamic Home Dashboard Streak**: Replaced hardcoded demo streak text with live streak tracking derived from active habits and consecutive task completion history.

## 🎨 UI/UX Polish & Visual Design
- **Live Timetable Alarm Badges**: Both Lecture and Clinic cards now visually reflect real-time alarm states with contextual color-coded badges:
  - `🔔 Start & End Armed` (Emerald green highlight)
  - `🔔 Start Armed` or `🏁 End Armed` (Individual active alerts)
  - `🔕 Alarms Off` (Subtle muted state)
  - `⚠️ Not Synced` (Call to action when schedule needs synchronization)
- **Modal Switch Controls**: Embedded refined Material 3 switches into the Timetable session detail sheet with tactile audio and haptic feedback.
- **Hierarchical Back-Gesture Flow**: Eliminates accidental app exits and ensures natural Android platform navigation expectations.

## 🐛 Bug Fixes & Refinements
- **Overdue Sweep Database Load Optimization**: Optimized `TaskViewModel.checkOverdueTasks()` to read directly from in-memory task state rather than issuing a full database query every 60 seconds, eliminating CPU and battery churn.
- **Analytics & Gamification Metric Integrity**: Corrected task completion metrics, 7-day activity charts, and XP awards in `LunaAnalyticsView` and `LunaHomeDashboardView` to exclude abandoned and missed tasks (`TaskStatus.FAILED_LOGGED`).
- **Foreground Timer Service Ticker Throttling**: Decoupled the 1-second countdown loop in `LunaFocusView` from the foreground service sync, throttling notification broadcasts to periodic intervals and mode transitions to reduce IPC overhead.
- **Alarm Scheduler Cancellation Handling**: Updated `LunaAlarmScheduler` to strictly evaluate `alarmOnStart` and `alarmOnFinish` flags, explicitly canceling pending intents when alarms are toggled off.

## ⚙️ Technical, Performance & Architecture
- **Strict AGENTS.md Conformance**:
  - Incremented `versionName` to `1.0.9` and `versionCode` to `109` in `app/build.gradle.kts`.
  - Dynamic attribution reflection in `BuildConfig.VERSION_NAME` via `LunaAttribution.APP_VERSION`.
  - Single-ABI signed release build restricted to `arm64-v8a`.
  - 100% unit test verification with all tests passing.

## 📦 Release Assets
- **Application Binary**: `Luna-v1.0.9-arm64-release.apk`
- **Target Architecture**: `arm64-v8a`
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35) / Compile SDK 36
