## 🌟 What's New & Enhancements
- **Complete Data Sanitization & Clean Source**: Stripped all pre-injected mock data, hardcoded sample tasks, and dentistry seed sessions from the codebase and application binaries.
- **Dynamic, Fully User-Driven Weekly Timetable**: `LunaTimetableScheduleView` now dynamically constructs schedules solely from user-created tasks possessing a recurring `weeklyDay` (supporting all 7 days: Mon through Sun), start time, duration, and room location.
- **Automatic Legacy Data Purge**: Integrated a startup purge in `LunaApplication` and `TaskRepository` that cleans existing databases of any legacy preinjected dental entries, cancels their lingering system alarms, and leaves user-created tasks pristine.
- **Dynamic Course & Category Legend**: Replaced static dentistry course catalogs with dynamic palettes extracted directly from the user's active timetable tasks and categories.
- **Clean, Universal Financial Architecture**: Generalized transaction and budget presets across `CreateTransactionSheet` and `LunaWealthView` to standard personal finance categories (Groceries, Utilities, Rent, Dining, Tech, Transport, Healthcare, Education) and replaced niche filter chips with universal "Bills & Rent".

## 🎨 UI/UX Polish & Visual Design
- **Full 7-Day Timetable Support**: Expanded the Timetable Day selector from 3 days to all 7 days (`ALL`, `MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`) with smooth horizontal touch-scrolling.
- **Universal Timetable Header**: Redesigned header to "WEEKLY SCHEDULE · TIMETABLE" with real-time academic/calendar term calculations and dynamic multi-metric stat cards.
- **Contextual Empty States**: Added clean illustrations and intuitive guidance when no timetable tasks are scheduled or when specific weekdays have no events.
- **Streamlined Settings Sheet**: Removed legacy hardcoded dental timetable import cards from the Settings sheet for a clean, professional settings experience.

## 🐛 Bug Fixes & Refinements
- **Zero Unwanted Task Injection**: Fixed the startup hook that repeatedly re-injected 13 mock dental tasks on every app boot.
- **Deprecated Broadcast Receiver**: Updated `TimetableReceiver` to safely deprecate external mock injection intents.
- **Alarm Cancellation on Purge**: Ensured all pre-injected task alarms are cancelled from Android's `AlarmManager` during the database sanitization sweep.

## ⚙️ Technical, Performance & Architecture
- **Strict AGENTS.md Conformance**:
  - Incremented `versionName` to `1.1.3` and `versionCode` to `113` in `app/build.gradle.kts`.
  - Dynamic attribution reflection in `BuildConfig.VERSION_NAME` via `LunaAttribution.APP_VERSION`.
  - Single-ABI signed release build restricted to `arm64-v8a`.
  - 100% unit test verification with all release tests passing.
  - Successfully deployed and verified on physical device `feb05c2c`.

## 📦 Release Assets
- **Application Binary**: `Luna-v1.1.3-arm64-release.apk`
- **Target Architecture**: `arm64-v8a`
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35) / Compile SDK 36
