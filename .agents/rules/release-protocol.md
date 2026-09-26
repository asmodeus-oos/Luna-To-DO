# Detailed Release & APK Attachment Rule

For every version created in the project:
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
