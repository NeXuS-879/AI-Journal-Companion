# In memory journal history verification

Date: 23 October 2026. Source: `a09afd8` plus the uncommitted history increment.

## Implemented behaviour

Each successful analysis creates a `JournalEntry` containing a stable UUID, creation timestamp, trimmed journal text, emotion and advice. `JournalViewModel` publishes a new history snapshot with the latest entry first. Failed requests, denied permission and draft editing preserve existing entries. Intentionally submitting the same text again creates a separate entry.

The Compose screen displays an empty-state message or lazy history cards with text, localized timestamp, emotion emoji and advice. The session limitation is visible. Activity recreation retains history through the existing ViewModel; a new ViewModel starts empty. There is no disk storage, database, backend history endpoint, sorting, search or deletion in this increment. History is not guaranteed after activity finish or process death.

## Command and results

Working directory: `android/AIJournalCompanion`.

```powershell
& 'C:\Program Files\Android\Android Studio\jbr\bin\java.exe' -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain testDebugUnitTest assembleDebug connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.liveBackend=true' --console=plain
```

Final result: `BUILD SUCCESSFUL` on the Android 17 Medium Phone emulator. All 16 JVM tests and 15 instrumented tests passed, with no skipped tests. The final run reused the already successful JVM task output from the first run; instrumented tests reran after their selector correction.

- ViewModel tests verify multi-entry fields and newest-first order, distinct IDs, timestamp bounds, immutable previous snapshots, intentional repeated text, history preservation across failed requests/permission denial/draft edits, and empty state for a new ViewModel.
- Compose tests verify the session warning, empty state, card content and scrolling through 30 synthetic entries.
- The opt-in live screen test submitted two synthetic entries through FastAPI and local Ollama and retained both after activity recreation. The existing live networking test also passed. These are real service calls, not fixtures.
- Existing input validation, loading, error, retry and networking tests also passed in the combined suite.

The backend was unchanged. Its previously recorded 16 passing tests remain historical evidence; the backend suite was not rerun for this Android-only change.

## Defects discovered during verification

The first instrumented run found a test assertion expecting a single JOY label even though several history entries correctly shared that emotion. The selector was changed to inspect the first matching label. A subsequent compile caught the missing `onFirst` test import; it was added. The final complete command above passed. Neither issue required a production-code workaround.

## Evidence and remaining checks

JUnit XML is generated under `app/build/test-results/testDebugUnitTest` and `app/build/outputs/androidTest-results/connected/debug`. The assessment report case TC-15 is now Pass; future algorithms and interactions remain Not run.

Manual force-stop/process-death behaviour was not separately exercised. Empty initialization is verified by the new-ViewModel unit test. No new screenshot is claimed for this phase. Real model emotion/advice wording is nondeterministic; tests check valid responses and retention rather than fixed model wording.
