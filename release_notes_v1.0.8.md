## 🌟 What's New & Enhancements
- **Refined Data Management Icons**: Exchanged and corrected the action button icons for data backup in the Settings sheet under `DATA MANAGEMENT`:
  - **Export All Data (JSON)** now accurately features the upward-pointing **Upload / Outward** icon (`UntitledIcons.Upload`), signifying backing up and transmitting data out of the application to external storage or cloud destinations.
  - **Import All Data (JSON)** now accurately features the downward-pointing **Download / Inward** icon (`UntitledIcons.Download`), signifying retrieving and pulling data into the application database.

## 🎨 UI/UX Polish & Visual Design
- **Intuitive Visual Metaphor**: Aligned iconography with user expectations and industry UI conventions where export represents saving outward and import represents bringing content inward.
- **Consistent Liquid Glass Styling**: Maintained consistent stroke widths, paddings, and Apple Liquid Glass aesthetics for both buttons.

## 🐛 Bug Fixes & Refinements
- **Icon Alignment Fix**: Resolved the inverted icon pairing where Export was showing a download arrow and Import was showing an upload arrow.

## ⚙️ Technical, Performance & Architecture
- **Strict AGENTS.md Conformance**:
  - Incremented `versionName` to `1.0.8` and `versionCode` to `108` in `app/build.gradle.kts`.
  - Dynamic attribution reflection in `BuildConfig.VERSION_NAME` via `LunaAttribution.APP_VERSION`.
  - Single-ABI signed release build restricted to `arm64-v8a`.

## 📦 Release Assets
- **Application Binary**: `Luna-v1.0.8-arm64-release.apk`
- **Target Architecture**: `arm64-v8a`
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35) / Compile SDK 36
