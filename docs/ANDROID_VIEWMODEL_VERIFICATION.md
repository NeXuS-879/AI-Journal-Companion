# Journal ViewModel and Screen Integration Verification

Date: 23 October 2026 (Australia/Perth).

## Implemented behaviour

`JournalViewModel` owns immutable `JournalUiState` through a private MutableStateFlow and public StateFlow. It validates input, coordinates permission decisions, starts a single API request in viewModelScope, and exposes loading, result and typed errors. Compose collects state with lifecycle awareness and renders a stateless screen. MainActivity provides an activity-scoped ViewModel with an injected JournalApi.

Loading appears immediately, duplicate requests are ignored, and editing/submission controls are disabled while busy. Errors retain the draft and re-enable Analyze for retry. Editing after completion clears the old result. Results display the backend emotion, a presentation-specific emoji and advice.

Input and result survive activity recreation in ViewModel memory. They are not written to disk or restored after process death. Journal history is not implemented in this increment.

## Automated verification

Executed with Android Studio's JDK 25 and Gradle wrapper 9.5.0, from `android/AIJournalCompanion`:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.liveBackend=true' --console=plain
```

The command used the explicit Java executable and `org.gradle.wrapper.GradleWrapperMain` with SDK/cache access. Final result: `BUILD SUCCESSFUL`.

| Suite | Results |
|---|---|
| JVM unit tests | 12 passed, 0 failed: one existing example, four API-model tests, seven new ViewModel tests |
| Connected emulator tests | 13 passed, 0 failed, 0 errors, 0 skipped on Medium Phone Android 17 |
| APK | Debug APK assembled and installed successfully |

The ViewModel tests verify invalid input never sends requests, code-point length, loading state, duplicate suppression, submitted-input stability, permission pending/grant/denial, all API error mappings, retry success, and clearing stale results when editing.

Compose tests verify blank input, whitespace correction, maximum length, visible loading with disabled controls, emotion/emoji/advice presentation, and connection failure with retained draft and retry enabled. Existing HTTP tests remain passing.

The opt-in live screen test types a synthetic entry, recreates MainActivity to check draft retention, taps Analyze, waits for a real FastAPI/Ollama response, and recreates MainActivity again to check result retention. The live API test also passes independently. Live tests grant permission through test automation.

## Manual fresh-install verification

After the test runner removed its application installation, the debug APK was installed and opened again. A synthetic draft was entered: `I finished my assignment and feel proud.`

1. Tapped Analyze with local network permission not yet granted.
2. Observed Android's Nearby devices permission dialog for AI Journal Companion.
3. Chose Allow.
4. The pending submission resumed automatically through FastAPI and local gemma:2b.
5. The app displayed `JOY`, a smiling emoji, and a one-line advice response. The original draft remained visible.
6. A screenshot was captured and visually inspected: input, button, emotion and advice were readable without clipping in the default portrait layout.

Screenshot: [Journal analysis result](screenshots/journal-analysis.png).

## Limits and remaining work

- Runtime permission denial is covered in ViewModel tests; permanently denied permission and permission-dialog rotation were not manually tested.
- Cold process restart resets state by design. No journal history or database exists yet.
- Cancellation is rethrown, and ViewModel lifecycle owns requests. The underlying HTTP operation can still remain blocked until its configured timeout.
- Physical-device operation, large font sizes and release/TLS deployment were not verified in this increment.
- Gradle emitted native-access and native-symbol stripping warnings; the final build and all tests succeeded.
