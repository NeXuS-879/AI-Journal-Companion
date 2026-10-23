# Android API Client Verification

Date: 23 October 2026 (Australia/Perth).

## Scope

Separate Kotlin API models and an HTTP client for `POST /analyze`. The Compose screen still shows the previous not-connected notice: ViewModel state, runtime permission prompting, and button/result integration are the next increment.

## Implementation

- `network/model/AnalyzeRequest.kt`: nonblank journal text, 5,000 Unicode code-point limit after trimming.
- `network/model/AnalyzeResponse.kt` and `Emotion.kt`: validated one-line advice and the six backend emotion labels.
- `JournalApi.kt`: injectable suspend contract.
- `HttpJournalApi.kt`: UTF-8 POST, IO dispatcher, explicit headers, timeout and error handling, bounded response size, disabled redirects, and connection cleanup.
- `AnalysisJsonCodec.kt`: escaped request text and strict success-response parsing.
- `ApiException.kt`: HTTP status, connection, timeout, permission, and invalid-response failures.
- BuildConfig: configurable `journalBackendUrl`, default `http://10.0.2.2:8000/`.
- Main manifest: Internet and Android 17 local network permission declarations. Debug manifest: HTTP enabled only for development builds.

## Executed verification

From `android/AIJournalCompanion`, using Android Studio's bundled JDK 25 and the Gradle wrapper:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.liveBackend=true' --console=plain
```

The command was invoked through `org.gradle.wrapper.GradleWrapperMain` with the explicit JDK executable and SDK/cache access. The final run reported `BUILD SUCCESSFUL`.

| Check | Actual result |
|---|---|
| Local JVM tests | 5 passed: existing example plus four API-model checks |
| Connected Android tests | 11 passed, 0 failures, 0 errors, 0 skipped on Medium Phone Android 17 |
| HTTP request/response | Actual loopback server verified POST path, UTF-8 JSON, quotes/newlines/emoji escaping, trimming and response parsing |
| Main-thread caller | Suspending API invoked from `Dispatchers.Main` succeeded because socket work ran on IO |
| HTTP failures | 422, 502, 503, 500 and 302 preserved as typed HTTP failures |
| Socket failures | Read timeout and refused connection distinguished |
| Invalid success payloads | Malformed JSON, non-object JSON, missing/extra fields, unknown emotion, numeric/blank/multiline advice and oversized body rejected |
| Permission guard | Simulated denied Android 17 permission produced `PermissionRequired` before connecting |
| Live FastAPI and gemma:2b | Emulator used the configured host address to POST a synthetic journal entry; a valid typed emotion/advice response returned |
| UI regression | All four existing journal screen tests passed |

The live inference test used a synthetic entry about completing an assignment. It asserted the response contract, not exact model wording or a particular emotion label. The networking tests granted local network permission through test automation; the actual app permission prompt is not implemented in this increment.

An initial verification invocation failed before tests because PowerShell split an unquoted Gradle property argument. Quoting the complete `-Pandroid.testInstrumentationRunnerArguments.liveBackend=true` argument resolved that command issue. A previous run of the initial transport tests also passed before the permission guard and live smoke test were added.

## References and limits

- Android local-network permission guidance: https://developer.android.com/privacy-and-security/local-network-permission
- Live inference is opt-in; normal test runs skip that single test and do not require FastAPI/Ollama.
- Real physical-device networking and release/TLS operation were not tested.
- Cancellation propagates, but the blocking HTTP operation can remain active until its socket timeout. No automatic retry is performed, avoiding duplicate submissions.
- This is incremental verification evidence; the assessment's final manual test report and performance evaluation remain separate deliverables.
