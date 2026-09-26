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
   - Synchronize commit messages, release APK filenames, and git tags with the updated version.

## 2. Release & Signing Protocols
- Never commit `release.keystore`, `*.jks`, or `keystore.properties` to version control.
- Restrict release builds to `arm64-v8a` architecture unless multi-ABI is explicitly requested.
