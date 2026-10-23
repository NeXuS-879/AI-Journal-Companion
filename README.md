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

The Android application now opens a journal entry screen with an `OutlinedTextField`, character count, and Analyze button. It rejects empty, whitespace-only, and over-limit entries using the backend's 5,000-character limit after trimming. Input is held only in the current screen's memory.

This first UI increment does not send requests yet: Analyze displays an explicit notice that analysis is not connected. Kotlin API models, networking, ViewModel state, and emotion/advice presentation are the next increment.

With an emulator or device connected, run the Compose interaction tests from the Android project root:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Current scope

- Repository and Git setup
- Python virtual environment and backend dependencies
- FastAPI `GET /` status route
- Validated `POST /analyze` request and response contract
- Modular Ollama client and analysis service boundary
- Constrained prompt and Pydantic JSON-schema response validation
- Backend contract tests
- Kotlin and Jetpack Compose journal input screen with validation and Analyze control
- Architecture and requirements documentation based on the supplied planning document

Not yet implemented:

- Journal result UI and in-memory history
- Android-to-FastAPI networking
- Required sorting, searching, data structures, chart, drag-and-drop, and local HTML help

## Assessment constraints

The required technology and features are recorded in [AGENTS.md](AGENTS.md). In particular, the custom Bubble Sort, Insertion Sort, Selection Sort, Binary Tree search, HashMap search, and Doubly Linked List search must remain visible, selectable implementations rather than being replaced by convenience library methods.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Requirements](docs/REQUIREMENTS.md)
- Planning source: `docs/CITEMS-APP-001_v1.0_AI_Journal_Companion_Planning.docx`
