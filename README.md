# AI Journal Companion

AI Journal Companion is an OOP 3 assessment project for an Android journaling application. The planned application accepts a journal entry, sends it to a local Python FastAPI backend, and will later use Ollama with `gemma:2b` to return a dominant emotion and one line of advice.

This repository contains the planning material, project rules, architecture and requirements documentation, a runnable Android Studio project, and a FastAPI backend integrated with local Ollama using `gemma:2b`.

## Repository layout

```text
AI-Journal-Companion/
|-- AGENTS.md
|-- README.md
|-- .gitignore
|-- docs/
|   |-- ARCHITECTURE.md
|   |-- REQUIREMENTS.md
|   `-- CITEMS-APP-001_v1.0_AI_Journal_Companion_Planning.docx
|-- backend/
|   |-- .venv/
|   |-- tests/
|   |-- models.py
|   |-- main.py
|   |-- ollama_client.py
|   |-- services.py
|   |-- requirements-dev.txt
|   `-- requirements.txt
`-- android/
    `-- AIJournalCompanion/
        |-- app/
        |-- gradle/
        |-- build.gradle.kts
        |-- settings.gradle.kts
        |-- gradlew
        `-- gradlew.bat
```

## Backend setup

Install Ollama, make sure the service is running, and obtain the required model:

```powershell
ollama pull gemma:2b
ollama list
```

From the repository root on Windows PowerShell, activate the environment and run FastAPI:

```powershell
.\backend\.venv\Scripts\Activate.ps1
python -m pip install -r .\backend\requirements.txt
uvicorn backend.main:app --reload
```

Install the test dependencies and run the backend contract tests with:

```powershell
python -m pip install -r .\backend\requirements-dev.txt
python -m pytest .\backend\tests -q
```

Open `http://127.0.0.1:8000/` to check the status route or `http://127.0.0.1:8000/docs` for FastAPI's generated API interface.

Expected status response:

```json
{"status":"AI Journal Companion backend is running"}
```

`POST /analyze` validates a REST/JSON contract with a journal `text` field, sends a constrained structured-output request to local Ollama, and validates the generated JSON before returning it to the caller. Ollama runs at `http://127.0.0.1:11434` and uses `gemma:2b` by default.

The defaults can be changed for a terminal session before starting FastAPI:

```powershell
$env:OLLAMA_BASE_URL = "http://127.0.0.1:11434"
$env:OLLAMA_MODEL = "gemma:2b"
```

Connection or model failures return HTTP `503`. A model response that does not match the required schema returns HTTP `502`.

Controlled emotion labels are `JOY`, `SADNESS`, `ANGER`, `FEAR`, `SURPRISE`, and `NEUTRAL`.

## Android setup

Open `android/AIJournalCompanion` as the project root in Android Studio. From that directory, the baseline can be verified with:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

The Android application opens a journal entry screen with an `OutlinedTextField`, character count, and Analyze button. It rejects empty, whitespace-only, and over-limit entries using the backend's 5,000-code-point limit after trimming.

Analyze now requests local network permission when required, sends the journal to FastAPI, and displays the returned emotion, emoji, and advice. `JournalViewModel` owns immutable input, loading, result, and error state exposed through StateFlow. Compose collects this state with lifecycle awareness. During analysis a progress indicator appears and submission/input controls are disabled to prevent duplicate requests; the screen remains scrollable. Errors retain the draft so the user can retry with Analyze.

Draft, result and journal history remain in ViewModel memory across activity recreation (such as rotation), but are not persisted after process death or a new app session. Changing the draft clears its previous result without removing saved entries.

Each successful analysis adds a `JournalEntry` containing the trimmed submitted text, emotion, advice, stable ID and timestamp. Scroll below the form to see the session history, newest first. Failures and denied permissions do not add entries. Repeated text can be intentionally submitted as a separate entry with a distinct ID. A lazy list renders history cards as they become visible; there is no database or disk storage.

### Android API client

`network/model` contains `AnalyzeRequest`, `AnalyzeResponse`, and the six-label `Emotion` enum. `network/JournalApi.kt` defines the injectable API interface. `HttpJournalApi` sends UTF-8 JSON to `POST /analyze` on `Dispatchers.IO`, validates the response, and reports HTTP, timeout, connection, permission, and invalid-response errors separately. It sends no requests until `analyze()` is called.

The default backend address is `http://10.0.2.2:8000/` (the Android emulator's route to this computer). Start FastAPI normally from the repository root. The emulator's `127.0.0.1` refers to the emulator itself, not the computer.

For a physical device on the same trusted network, configure the computer's LAN address in Android Studio's Gradle settings, or build with a local property override:

```powershell
.\gradlew.bat assembleDebug '-PjournalBackendUrl=http://YOUR_COMPUTER_LAN_IP:8000/'
```

For physical-device access only, run FastAPI on a reachable interface using `python -m uvicorn backend.main:app --host 0.0.0.0 --port 8000` and allow the port on the trusted Windows private network. Never expose the development service publicly. Do not commit machine-specific addresses.

Debug builds permit HTTP for the local assessment server; release builds block cleartext traffic. Because this project targets SDK 37, Android 17 requires the declared `ACCESS_LOCAL_NETWORK` runtime permission before client calls. Analyze checks this permission and opens Android's permission dialog when needed. Granting permission resumes the pending submission; denial shows an explanation without sending the entry. If repeated denial prevents the dialog from appearing, allow Nearby devices in this app's Android settings. Earlier Android versions need only the normal `INTERNET` declaration.

The client has a 5-second connect timeout and 130-second read timeout to allow the backend's 120-second Ollama request. Cancellation is propagated and connections are closed, but a blocked HTTP operation can take until its timeout to return.

With an emulator or device connected, run the Compose interaction tests from the Android project root:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

The default suite uses local HTTP fixtures and skips the optional live inference check. With FastAPI and Ollama running, opt in to an actual emulator-to-backend analysis:

```powershell
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.liveBackend=true'
```

The instrumented networking/live tests grant the local network permission through test automation. The app also has its own user-facing permission flow, and ViewModel tests cover pending, granted and denied decisions. The optional live screen test submits through the actual Analyze button and verifies state retention across activity recreation.

## Current scope

- Repository and Git setup
- Python virtual environment and backend dependencies
- FastAPI `GET /` status route
- Validated `POST /analyze` request and response contract
- Modular Ollama client and analysis service boundary
- Constrained prompt and Pydantic JSON-schema response validation
- Backend contract tests
- Kotlin and Jetpack Compose journal input screen with validation and Analyze control
- Separate Kotlin API models, a suspend API interface, and an HTTP/JSON client for `POST /analyze`
- ViewModel/StateFlow input, loading, result and error management; local-network permission prompting; real Analyze-button integration and emotion/emoji/advice display
- In-memory journal history with timestamped entry cards, newest-first order, and configuration-change retention
- Architecture and requirements documentation based on the supplied planning document

Not yet implemented:

- Required sorting, searching, data structures, chart, drag-and-drop, and local HTML help

## Assessment constraints

The required technology and features are recorded in [AGENTS.md](AGENTS.md). In particular, the custom Bubble Sort, Insertion Sort, Selection Sort, Binary Tree search, HashMap search, and Doubly Linked List search must remain visible, selectable implementations rather than being replaced by convenience library methods.

## Documentation

- [Working assessment test report](docs/AI_Journal_Companion_Test_Report.docx) - populated from the supplied Word template and maintained after tested development increments. Future features remain marked `Not run`.
- [Test report maintenance](docs/testing/README.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Requirements](docs/REQUIREMENTS.md)
- Planning source: `docs/CITEMS-APP-001_v1.0_AI_Journal_Companion_Planning.docx`
