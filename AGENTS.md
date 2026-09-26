# Luna Project Rules & Guidelines

## 1. Version Bump Rule
For every set of changes or feature modifications made to the project:
1. **Version Name (`versionName`)**:
   - Increment patch by `0.0.1` (e.g. `1.0.1` -> `1.0.2`).
   - If patch exceeds 9 (`> 9`), roll over: reset patch to `0` and increment minor by `1` (e.g. `1.0.9` -> `1.1.0`).
   - If minor exceeds 9 (`> 9`), roll over: reset minor to `0` and increment major by `1` (e.g. `1.9.9` -> `2.0.0`).
2. **Version Code (`versionCode`)**:
   - Always increment integer `versionCode` by 1 (e.g. `101` -> `102`).
3. **Location**:
   - Update in [`app/build.gradle.kts`](app/build.gradle.kts).
4. **Consistency**:
   - Synchronize commit messages, release APK filenames, git tags, and GitHub releases with the updated version.

## 2. Detailed Release & APK Attachment Rule
For each version created:
1. **Signed APK Compilation**:
   - Build signed release APK restricted to `arm64-v8a`: `app/build/outputs/apk/release/app-release.apk`.
   - Copy to project root with standardized name: `Luna-v<versionName>-arm64-release.apk`.
2. **Detailed Release Documentation**:
   - Every release MUST include a detailed, categorized markdown release body covering:
     - 🌟 **What's New & Enhancements**
     - 🎨 **UI/UX Polish & Visual Design**
     - 🐛 **Bug Fixes & Refinements**
     - ⚙️ **Technical, Performance & Architecture**
     - 📦 **Release Assets** (specifying architecture and min SDK)
3. **GitHub Release Publication**:
   - Commit changes, create annotated tag `v<versionName>`, and push to `origin/main --tags`.
   - Publish GitHub Release using `gh release create v<versionName> "Luna-v<versionName>-arm64-release.apk" --title "..." --notes "..."`.
   - **MANDATORY**: Always attach the compiled signed APK (`Luna-v<versionName>-arm64-release.apk`) to the GitHub release.
4. **Local Device Deployment**:
   - Whenever an Android physical device or emulator is connected via ADB, install the updated signed APK via `adb install -r -d` and launch for immediate user validation.

## 3. App Settings & UI Version Tracking Rule
- The version string displayed in the App Settings "About" section and across any UI surface MUST dynamically derive from `BuildConfig.VERSION_NAME` (via `LunaAttribution.APP_VERSION`).
- Hardcoding static version numbers in UI components or data models is strictly prohibited.
- When `versionName` or `versionCode` is updated in `app/build.gradle.kts`, the Settings About section must automatically reflect the new version code and name upon build without manual edits.

## 4. Security & Signing Protocols
- Never commit `release.keystore`, `*.jks`, or `keystore.properties` to version control.
- Restrict release builds to `arm64-v8a` architecture unless multi-ABI is explicitly requested.
