# Wireframe UI verification

Date: 23 October 2026. Source: `ec8d69e` plus UI working-tree changes.

## Scope and reference

The supplied `AIJournalUiWireframe.drawio` is retained unchanged in `docs/wireframes`. Source and retained-copy SHA256: `86917d1c2449c61cc974fb327b0fbd8663492e95f438f6b29af65ae76880e13a`.

Journal and History now have separate bottom-navigation destinations inside a Scaffold with a top app bar. Journal uses a scrollable form and separate last-entry/analysis cards; History uses a lazy list and session count. Both read the same ViewModel. Drafts and requests are not recreated when switching tabs; tab selection and both scroll holders use saveable Compose state. Tab changes hide the software keyboard. The latest successful reflection is read from history, so editing a new draft does not hide it.

Search selectors, search/sort actions, chart, Help and the deletion area are disabled layout placeholders with visible coming-later guidance. Binary Tree and Bubble Sort are illustrative field values only, not active strategies. History cards retain their existing full text and advice; detail popups, draggable handles and gestures are not implemented. No custom algorithms or new assessment functionality were introduced. The blue/grey light palette follows the supplied layout while dark mode remains supported.

## Automated verification

Working directory: `android/AIJournalCompanion`.

```powershell
& 'C:\Program Files\Android\Android Studio\jbr\bin\java.exe' -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain testDebugUnitTest assembleDebug connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.liveBackend=true' --console=plain
```

The first complete run passed all 16 JVM tests and 17 Android 17 Medium Phone instrumented tests, with no skipped tests. After entry-count pluralization and keyboard handling were refined, the final complete run also passed all 16 JVM and 17 instrumented tests with no skips: `BUILD SUCCESSFUL in 4m 22s`. Instrumentation XML reports 17 tests, 0 failures, 0 errors and 0 skipped.

- Existing validation, loading, success, failure and retry Compose tests were adapted to the new layout and `Analyse entry` label.
- The navigation test submits through a fake API, edits a new draft, switches tabs and checks that the draft and previous advice are still displayed.
- The placeholder test asserts that Help, emotion/method/sort selectors, search, sort, chart and delete controls are disabled. This does not count as implementing or verifying the future algorithms.
- Existing history empty-state and 30-entry lazy-scroll tests now navigate to History first.
- The real screen-to-FastAPI/Ollama test performs two analyses, checks history retention after recreation and verifies recreation keeps History selected. The separate real networking test also remains enabled.

The backend was unchanged and its test suite was not rerun. Previous backend results and screenshot E1 remain historical evidence. The final report retains TC-16 through TC-25 as Not run; TC-26 and TC-27 cover only navigation and inactive placeholders.

## Limitations

No new sorting, searching, chart, drag deletion, local HTML help or entry-detail behaviour is enabled. History is still memory-only. The automated suite uses one Android 17 emulator at its configured font scale; other screen sizes, enlarged fonts, TalkBack and full dark-mode interaction are not claimed as tested.

## Visual and manual evidence

The rebuilt debug APK was installed and launched outside the test runner. A synthetic entry, "I finished my assignment and feel proud.", returned JOY and real advice through the existing backend. `docs/screenshots/wireframe-journal.png` records the Journal layout and separate entry/analysis cards. Switching to History showed the same entry, full advice, singular entry count, session warning and disabled future controls, recorded in `docs/screenshots/wireframe-history.png`. Both screenshots were visually inspected for complete content and navigation clearance. No fabricated entries or advice are inserted into production state.

Standalone emulator startup was slow: `am start -W` timed out, while the activity eventually displayed after approximately 94 seconds according to ActivityTaskManager. UI hierarchy dumps during transitions sometimes returned null. No app crash was recorded in the AndroidRuntime check. The screenshot and later hierarchy were captured only after the app actually rendered; startup performance is not claimed as satisfactory or formally measured by the passing interaction suite.

The test report was regenerated from the unchanged template. The bundled document renderer could not find LibreOffice, so read-only Word PDF export plus bundled Poppler was used to inspect both pages. All package parts except `word/document.xml` remain byte-identical to the template; no Word save/reformat was used.
