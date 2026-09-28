## 🌟 What's New & Enhancements
- **Tab Rebrand: Wealth → Finance**:
  - Rebranded the bottom navigation tab, tab models, and primary financial dashboard from "Wealth" to "Finance" across all app surfaces.
  - Updated `LunaNavTab.FINANCE` enum and navigation bar labels with seamless backward compatibility.
  - Renamed the primary view to [`LunaFinanceView`](file:///E:/Luna/app/src/main/java/com/luna/app/ui/views/LunaFinanceView.kt) while preserving legacy alias compatibility.

## 🎨 UI/UX Polish & Visual Design
- **120fps Silky Smooth Horizontal Pager Swiping**:
  - Eliminated the "sticky page" touch interception issue when swiping to and from the Finance tab.
  - Converted nested horizontally scrolling rows (Accounts carousel, Savings Goals carousel, Filter Chips row) into responsive, wrapped `FlowRow` layouts, granting `HorizontalPager` 100% unimpeded drag ownership.
  - Single-swipe tab transitions now respond instantly with natural deceleration and zero resistance or gesture conflicts.
- **Virtualized High-Performance Architecture**:
  - Migrated the financial dashboard from an un-virtualized `Column(Modifier.verticalScroll())` to an optimized `LazyColumn` with stable item keying (`TransactionEntity.id`).
  - Offloaded off-screen transaction cards, progress indicators, and budget rows from the composition tree during swiping.
- **Universal Financial Quick-Log Presets**:
  - Updated quick-add placeholders to universal everyday examples (e.g., `"Groceries 42.50 #food"`).
  - Modernized icons to AutoMirrored Compose standards.

## 🐛 Bug Fixes & Refinements
- **Fixed Multi-Swipe Gesture Conflict**: Resolved touch event consumption where nested `horizontalScroll` components intercepted horizontal drag gestures intended for `HorizontalPager`.
- **Jank-Free Memoization**: Cached expensive aggregate net worth calculations, start-of-month calendar computations, monthly income/expense totals, and `DecimalFormat`/`SimpleDateFormat` instances using Compose `remember(keys)`.

## ⚙️ Technical, Performance & Architecture
- **Strict AGENTS.md Conformance**:
  - Incremented `versionName` to `1.1.4` and `versionCode` to `114` in [`app/build.gradle.kts`](file:///E:/Luna/app/build.gradle.kts).
  - Dynamic attribution reflection in `BuildConfig.VERSION_NAME` via `LunaAttribution.APP_VERSION`.
  - Single-ABI signed release build restricted to `arm64-v8a`.
  - 100% unit test verification with all release tests passing.
  - Successfully deployed and verified on physical device `feb05c2c`.

## 📦 Release Assets
- **Application Binary**: `Luna-v1.1.4-arm64-release.apk`
- **Target Architecture**: `arm64-v8a`
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35) / Compile SDK 36
