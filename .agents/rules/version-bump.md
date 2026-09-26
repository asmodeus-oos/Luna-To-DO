# Version Bump Rule

For every code change or feature modification made to the project:
1. Bump version name in `app/build.gradle.kts`:
   - Increment patch by `0.0.1` (e.g. `1.0.1` -> `1.0.2`).
   - If patch exceeds 9 (`> 9`), roll over: reset patch to `0` and increment minor by `1` (e.g. `1.0.9` -> `1.1.0`).
   - If minor exceeds 9 (`> 9`), roll over: reset minor to `0` and increment major by `1` (e.g. `1.9.9` -> `2.0.0`).
2. Always increment `versionCode` by 1 (e.g. `101` -> `102`).
3. Maintain synchronization in commit messages, release APKs, and tags.
