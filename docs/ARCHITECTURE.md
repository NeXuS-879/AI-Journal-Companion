# AI Journal Companion Architecture

## Purpose and scope

This document defines the planned architecture for the AI Journal Companion OOP 3 assessment. It translates the supplied planning document into module boundaries that support incremental delivery, visible OOP design, independently testable custom algorithms, and REST/JSON communication.

The current implementation includes the repository foundation, a Kotlin and Jetpack Compose journal input screen, a validated FastAPI analysis contract, and local Ollama integration using `gemma:2b`. Android-to-FastAPI networking, result presentation, and journal history are the next development increments.

## System context

The final application will contain three locally operated parts:

```text
Android application (Kotlin and Jetpack Compose)
        |
        | HTTP REST requests and JSON responses
        v
FastAPI backend (Python)
        |
        | Local HTTP request
        v
Ollama using gemma:2b
```

The Android application owns the user experience and session journal history. FastAPI validates the network boundary, constructs a controlled AI prompt, communicates with Ollama, normalises the model output, and returns a small structured response. Ollama performs local inference only.

## Repository structure

```text
AI-Journal-Companion/
|-- AGENTS.md                 Permanent assessment development rules
|-- README.md                 Setup, operation, and project status
|-- docs/                     Planning and maintained technical documents
|-- backend/                  Python FastAPI service and contract tests
`-- android/
    `-- AIJournalCompanion/   Android Studio Gradle project root
```

The backend contains explicit API models, an analysis service boundary, route tests, dependency files, and an ignored `.venv` virtual environment. The Android project is a single-module Compose application. `MainActivity` hosts `ui/journal/JournalScreen.kt`, which separates temporary input state and validation from a stateless form. The form contains the required `OutlinedTextField` and Analyze button. Screen text lives in Android string resources.

This first UI increment keeps draft text only in screen memory and checks the same trimmed 5,000-character limit as the backend. Valid submission displays an explicit not-connected notice. Network calls, ViewModel state, results, and session history remain future increments; no generated analysis is shown by this screen yet.

## Planned Android architecture

The Android project should use focused packages or modules with dependencies directed toward domain abstractions:

- **UI and presentation:** Compose screens and reusable components for journal input, progress and error feedback, history, algorithm selection, search results, the pie chart, drag-and-drop deletion, and help.
- **State management:** A screen-level state holder or ViewModel coordinates immutable UI state and user actions. Network calls must not block the main thread.
- **Domain models:** A `JournalEntry` model represents journal text, dominant emotion, advice, and any session identifier or timestamp required by the UI.
- **Networking:** Explicit request and response DTOs plus an API client isolate REST/JSON details from the UI.
- **Sorting strategies:** Separate custom implementations of Bubble Sort, Insertion Sort, and Selection Sort operate on journal entries without using built-in sorting as the assessed logic.
- **Search strategies and structures:** Separate Binary Tree, HashMap, and Doubly Linked List search implementations share a small search contract so the UI can select them consistently.
- **Visualisation:** Frequency calculation is separated from Compose Canvas rendering so counts and angles can be tested independently.
- **Help:** A packaged local `help.html` asset is displayed through a WebView or suitable dialog without requiring internet access.

Interfaces or strategy abstractions should be introduced where they make the three sorting or three searching implementations interchangeable and testable. Composition should connect these parts; UI classes should not contain algorithm implementations or raw network code.

## Planned backend architecture

The backend is organised into the following responsibilities:

- **Application entry point:** creates and configures the FastAPI application and routes.
- **API models:** Pydantic request and response models validate journal text and the structured emotion/advice result.
- **Analysis service:** isolates the API route from the selected analysis provider and maps provider failures into application-level errors.
- **Ollama client:** owns the constrained prompt, local HTTP communication, JSON-schema request, response parsing, and Pydantic validation for the required `gemma:2b` model.

These components are implemented. The active service creates an Ollama client using `OLLAMA_BASE_URL` and `OLLAMA_MODEL` environment settings, with local defaults of `http://127.0.0.1:11434` and `gemma:2b`.

## REST and JSON boundary

The analysis operation is `POST /analyze`.

Conceptual request:

```json
{
  "text": "I completed an important task today."
}
```

Conceptual successful response:

```json
{
  "emotion": "JOY",
  "advice": "Keep building on what went well today."
}
```

Requests contain trimmed, non-empty journal text with a maximum length of 5,000 characters. Successful responses use one of the controlled labels `JOY`, `SADNESS`, `ANGER`, `FEAR`, `SURPRISE`, or `NEUTRAL`, plus non-empty advice of no more than 300 characters. These rules must remain consistent across Android code, backend tests, README instructions, and help content.

FastAPI sends a non-streaming request to Ollama's `POST /api/generate` endpoint. The Pydantic response schema is supplied through Ollama's structured-output `format` field, and temperature is set to zero for more deterministic output. The returned JSON string is validated again before it crosses the FastAPI boundary.

Connection, timeout, service, or missing-model failures become HTTP `503`. Generated content outside the required schema becomes HTTP `502`. Tests use injected services and mocked HTTP sessions so success and error paths remain deterministic without requiring live inference.

The existing `GET /` endpoint is only a health/status check and does not call Ollama.

## Runtime data and lifecycle

Journal entries remain in Android application memory for the current session, matching the assessment scope. Persistent database storage is not part of the planned release. The local help and user documentation must make this limitation clear.

When a user analyses text:

1. Compose validates that usable journal text is present.
2. The state holder exposes a loading state and asks the network layer to send JSON to FastAPI.
3. FastAPI validates the request and asks the analysis service to process it.
4. The analysis service sends a controlled prompt to local Ollama using `gemma:2b`.
5. The backend validates and normalises the result into `emotion` and `advice` fields.
6. Android displays the result and adds a `JournalEntry` to in-memory history.
7. Failures become clear user-facing errors without freezing the UI or adding an invalid entry.

## Required algorithms and data structures

Bubble Sort, Insertion Sort, and Selection Sort must each be custom, independently selectable implementations. Built-in sorting may only be used in tests as a comparison oracle.

Binary Tree search, HashMap search, and Doubly Linked List search must each remain visible, independently selectable search approaches. The Binary Tree and Doubly Linked List must be purpose-built structures. The HashMap strategy may use Kotlin's map storage while explicitly constructing and querying its emotion index.

All strategies should share consistent inputs and outputs so behavioural and performance comparisons are meaningful. Duplicate emotion values must be handled because multiple journal entries can share the same emotion.

## Security and deployment constraints

- Run FastAPI and Ollama on a trusted local development network only.
- Do not expose the Uvicorn development server publicly.
- Do not commit secrets, local IP configuration, virtual environments, generated APKs, or build output.
- Use a network-accessible Uvicorn host only when a physical Android device requires it, and document the local IP and firewall setup without hard-coding machine-specific values.
- Treat journal content as sensitive even though the assessment uses local processing.

## Incremental implementation order

1. Repository, documentation, virtual environment, Android baseline, and minimal FastAPI status route. Completed.
2. Pydantic request/response contract and a tested `POST /analyze` boundary without Android coupling. Completed.
3. Ollama `gemma:2b` client, controlled prompt, response validation, and backend error handling. Completed.
4. Kotlin and Jetpack Compose core journal workflow and in-memory model.
5. Android-to-FastAPI REST/JSON integration.
6. Custom sorting algorithms and selectable controls.
7. Binary Tree, HashMap, and Doubly Linked List search and selectable controls.
8. Compose Canvas pie chart, drag-and-drop deletion, and local HTML help.
9. Formal integration, regression, performance, documentation, and handover evidence.

## Verification approach

Backend routes should be tested independently before Android integration. Domain calculations, custom algorithms, and data structures should have focused tests, while Compose interactions and the complete local AI flow require integration or manual evidence. Results must only be reported as passing when the relevant command or procedure was actually executed.
