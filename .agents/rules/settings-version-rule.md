# App Settings Version Tracking Rule

- The version string displayed in the App Settings "About" section and across any UI surface MUST dynamically derive from `BuildConfig.VERSION_NAME` (via `LunaAttribution.APP_VERSION`).
- Hardcoding static version numbers in UI components or data models is strictly prohibited.
- When `versionName` or `versionCode` is updated in `app/build.gradle.kts`, the Settings About section must automatically reflect the new version code and name upon build without manual edits.
