# Luna 🌙

> A humane, minimalist to-do list built with pure native **Kotlin** and **Jetpack Compose**, inspired by the craft, micro-interactions, and aesthetics of **[dona.ai](https://dona.ai)**.

---

## ✨ Core Task Management Features

### 1. Complete Task Lifecycle
* **Create, Edit, Delete, Duplicate**: Create lightweight tasks with inline quick-add or deep edit mode. Duplicate tasks with all subtasks preserved via a single tap.
* **Subtasks & Checklists**: Interactive subtask lists with individual completion toggles, progress bars (`✓ 2/5`), and instant inline addition.
* **Task Dependencies**: Block task B until task A is done (`dependsOnTaskId`). Blocked tasks display an amber `🔒 Blocked by: [Task Title]` badge and locked checkbox until the blocking task is completed.
* **Priority Levels**: **P1 (Urgent)**, **P2 (High)**, **P3 (Medium)**, **P4 (Low)**, and None with dedicated colored pill badges.
* **Due Dates & Start Dates**: Flexible due dates, start dates, and time stamps with visual past-due alerts and natural relative formatting.
* **Recurring Tasks**: Automated recurrence engine (`Daily`, `Weekdays`, `Weekly`, `Monthly`). Completing a recurring task automatically schedules the next occurrence with fresh checklists.
* **Tags & Cross-Cutting Labels**: Cross-cutting tag categorization with `#tag` notation and dedicated pill chips.
* **Markdown Notes**: Support for headings (`#`, `##`), checklists (`- [ ]`, `- [x]`), bullet points, bold (`**text**`), italics, and code blocks.
* **File & Image Attachments**: Metadata tracking and attachment badges on task cards.
* **Task Templates**: Reusable multi-step workflows and checklists (pre-seeded with *Weekly Review* and *Bug Fix Workflow*). Any task can be saved as a template.
* **Bulk Actions (Multi-Select)**: Long-press to activate multi-select mode. Floating pill action bar for bulk complete, bulk due date, bulk priority, and bulk delete.
* **Natural Language Parsing**: Live parsing in the create sheet (`"Call John tomorrow 3pm #work p1"` extracts title, date, time, tags, and priority with real-time chip previews).

---

## 🎨 Sensory Micro-Interactions & Themes

* **Tactile Circular Checkbox**: Custom Canvas-drawn circular checkmark with animated path interpolation (`PathMeasure`) and squish-and-pop spring physics.
* **3-Tier Theme Engine**:
  * **Light Theme**: Clean paper porcelain background (`#F5F6F8`), deep graphite typography, electric blue accent (`#008FFD`).
  * **Slate Dark Theme**: Deep charcoal card surfaces (`#202328`) with soft slate borders.
  * **True OLED Black Theme**: Pure AMOLED `#000000` pitch black for battery efficiency and contrast.
* **Procedural Audio Synthesis**: Dual-tone harmonic chimes on task completion and soft pops on uncheck generated via Android `AudioTrack` without external audio assets.

---

## 🏗️ Architecture & Tech Stack

* **UI**: 100% Jetpack Compose (Material3 + Custom Luna Design System)
* **Language**: Kotlin 2.0.20 + Kotlin Compose Compiler Plugin
* **Persistence**: AndroidX Room 2.6.1 + KSP (`TaskEntity`, `SubtaskEntity`, `TaskTemplateEntity`, `TaskWithDetails`)
* **Preferences**: AndroidX DataStore Preferences 1.1.1
* **Concurrency**: Kotlin Coroutines & Reactive `StateFlow`
* **Audio Engine**: Custom procedural `TactileSoundPlayer` via `AudioTrack`

---

## 🚀 Building & Running

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.luna.app/.MainActivity
```
