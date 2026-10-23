# Android Journal Input Verification

Date: 23 October 2026 (Australia/Perth).

## Scope

First Android UI increment: journal input and Analyze button. The Android screen does not send network requests yet. A valid submission displays an explicit notice that analysis is not connected. This report is incremental development evidence, not the final assessment test report.

## Environment and command

- Android project: `android/AIJournalCompanion`.
- Device: running Medium Phone emulator, Android 17.
- JDK: Android Studio bundled JDK 25.
- Gradle wrapper: 9.5.0.
- Tasks: `testDebugUnitTest assembleDebug connectedDebugAndroidTest --console=plain`.
- Executed through `org.gradle.wrapper.GradleWrapperMain` with the Android Studio Java executable. Elevated SDK/cache access was needed after the restricted run encountered compiler daemon access issues.

## Results

The final Gradle run reported `BUILD SUCCESSFUL`. The generated example unit test passed, the debug APK was assembled, and all five connected tests passed with none skipped or failed: the existing package-context check and the four journal UI tests below.

| Test | Input or action | Expected | Actual |
|---|---|---|---|
| Blank submission | Tap Analyze with empty input | Show required-entry error | Passed |
| Whitespace and correction | Submit spaces, then replace with usable text | Reject spaces; clear error after correction | Passed |
| Valid submission | Submit `I feel proud.` | Show explicit not-connected notice | Passed |
| Oversized submission | Submit 5,001 non-whitespace characters | Show length error; do not show submission notice | Passed |

The updated debug APK was then installed and MainActivity launched successfully on the emulator. A captured screenshot was visually inspected: heading, instruction text, outlined multiline field, character count, and Analyze button were visible without clipping in the default portrait layout.

## Issues encountered

- Initial UI test compilation failed because `assertDoesNotExist` was imported as a top-level function; it is an interaction member in this Compose test version. The unnecessary import was removed and the full build/test run passed.
- The emulator test runner removed its installed application after execution. The debug APK was reinstalled and opened for the visual check.
- Gradle emitted a Java native-access warning; it did not prevent the successful final run.

## Remaining verification

Future increments must test the real Android-to-FastAPI request, loading state, emotion/emoji/advice result, connection failure, configuration changes, and journal history. This increment does not establish end-to-end AI behaviour from Android.
