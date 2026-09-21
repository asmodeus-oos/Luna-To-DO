# Luna 🌙

> A humane, minimalist productivity & task management suite built with pure native **Kotlin 2.0** and **Jetpack Compose**, inspired by the craft, micro-interactions, and aesthetic elegance of **[dona.ai](https://dona.ai)**.

---

## 🚀 Release v1.0.0 Highlights

* **🎨 Liquid Glass Design System**: Frosted glass top headers, view selectors, action sheets, and floating navigation bars with specular light borders and real-time backdrop blur.
* **📱 Adaptive App Drawer Icon & Native Splash Screen**: 
  * Features the transparent brand logo (`ic_main_icon_logo_removebg_black`).
  * Optimized 80dp adaptive icon scaling with 14dp safe-zone insets.
  * Native Android 12+ splash screen (`windowSplashScreenAnimatedIcon`) with 70% viewport coverage and zero cropping.
* **🌊 Fluid Liquid Glass Bottom Navigation Bar**:
  * Selected tab displays icon + label side-by-side inside an expanded pill with generous padding.
  * Smooth sliding liquid bubble indicator powered by spring physics (`dampingRatio = 0.75`).
* **📌 Sticky View Selector**:
  * Pinned top section in Tasks view holding List, Calendar, Habits, Routines, Search, and Templates subtabs.
  * Remains fixed below the header while task items scroll smoothly beneath it.
* **🌗 High-Contrast Light & Dark Themes**:
  * High contrast dark slate typography (`#18181B`), background tint (`#E4E4E8`), and borders (`#C4C4C8`) for light mode subtabs and habit pills.
  * Pure AMOLED `#000000` pitch-black dark mode option for battery efficiency.
* **⚡ Task Options & Checkbox Craft**:
  * Trailing 4-dot menu button styled as a rounded rectangle with checkbox roundness (`8dp`) and 4-dot grid icon (`GridDots4`).
  * Custom Canvas-drawn circular checkmark with animated path interpolation (`PathMeasure`) and squish-and-pop spring physics.
* **⏱️ Flexible Focus Timer**:
  * Defaults to "No Task Selected" for instant general focus sessions.
  * Selective task picker modal to assign or switch active focus targets on the fly.
* **📅 Weekly Recurrence & Alarms**:
  * Weekly day and time picker with start/finish notification alarm toggles.
  * Automated Room DB migration (v6 → v7).

---

## ✨ Core Features

### 1. Complete Task Lifecycle
* **Create, Edit, Delete, Duplicate**: Inline quick-add or deep edit sheets. Duplicate tasks with all subtasks preserved via a single tap.
* **Subtasks & Checklists**: Interactive subtask lists with individual completion toggles, progress bars (`✓ 2/5`), and instant inline addition.
* **Task Dependencies**: Block task B until task A is done (`dependsOnTaskId`). Blocked tasks display an amber `🔒 Blocked by: [Task Title]` badge and locked checkbox.
* **Priority Levels**: **P1 (Urgent)**, **P2 (High)**, **P3 (Medium)**, **P4 (Low)**, and None with dedicated colored pill badges.
* **Due Dates & Start Dates**: Flexible due dates, start dates, and time stamps with visual past-due alerts and natural relative formatting.
* **Recurring Tasks**: Automated recurrence engine (`Daily`, `Weekdays`, `Weekly`, `Monthly`). Completing a recurring task automatically schedules the next occurrence with fresh checklists.
* **Tags & Cross-Cutting Labels**: Cross-cutting tag categorization with `#tag` notation and dedicated pill chips.
* **Markdown Notes**: Support for headings (`#`, `##`), checklists (`- [ ]`, `- [x]`), bullet points, bold (`**text**`), italics, and code blocks.
* **Task Templates**: Reusable multi-step workflows and checklists. Any task can be saved as a template.
* **Bulk Actions (Multi-Select)**: Long-press to activate multi-select mode. Floating pill action bar for bulk complete, bulk due date, bulk priority, and bulk delete.
* **Natural Language Parsing**: Live parsing in the create sheet (`"Call John tomorrow 3pm #work p1"` extracts title, date, time, tags, and priority with real-time chip previews).

---

## 🏗️ Architecture & Tech Stack

* **UI Framework**: 100% Jetpack Compose (Material3 + Custom Luna Liquid Glass Design System)
* **Language & Tooling**: Kotlin 2.0.20 + Compose Compiler Plugin
* **Persistence**: AndroidX Room 2.6.1 + KSP (`TaskEntity`, `HabitEntity`, `RoutineEntity`, `TaskTemplateEntity`, `TaskWithDetails`)
* **Database Migrations**: Automated Room DB Migrations (v1 through v7)
* **Preferences**: AndroidX DataStore Preferences 1.1.1
* **Concurrency**: Kotlin Coroutines & Reactive `StateFlow`
* **Audio Engine**: Custom procedural `TactileSoundPlayer` via Android `AudioTrack`

---

## 🛠️ Building & Installing

### Signed Release Build
```powershell
# Build signed release APK
.\gradlew.bat assembleRelease

# Install on connected Android device via ADB
adb install -r app/build/outputs/apk/release/app-release.apk

# Launch Luna App
adb shell am start -n com.luna.app/.MainActivity
```

---

## 📜 Author & License

* **Author**: Dr. Mohamed Elsayed ([Asmodeus-OOS](https://github.com/asmodeus-oos))
* **Copyright**: Copyright © 2026 Dr. Mohamed Elsayed (Asmodeus-OOS). All Rights Reserved.
