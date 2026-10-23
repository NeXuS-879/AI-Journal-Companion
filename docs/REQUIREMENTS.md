# AI Journal Companion Requirements

## Purpose and authority

These requirements are derived from the supplied AI Journal Companion planning document for the OOP 3 assessment. They define the baseline that implementation, tests, help content, and handover evidence must follow. Mandatory technologies and assessed implementations may not be substituted for easier alternatives.

## Scope

The product is an educational Android application that lets a user enter journal text, receive locally generated emotion feedback and advice, retain analysed entries during the current session, and interact with those entries using specified algorithms, data structures, visualisation, deletion, and help features.

Journal persistence across process restarts, cloud AI services, public production deployment, and non-required enhancements are outside the current scope unless approved and documented as a change.

## Functional requirements

| ID | Requirement | Acceptance summary |
|---|---|---|
| FR-01 | Journal entry input | The Android Compose interface accepts free-text journal content and prevents an unusable submission. |
| FR-02 | Emotion analysis | Android sends journal text to a Python FastAPI backend, which later uses local Ollama with `gemma:2b`. |
| FR-03 | Advice generation | A successful analysis returns one dominant emotion and one line of advice in a validated JSON response. |
| FR-04 | Journal history | Analysed entries retain text, emotion, and advice in memory and are displayed for the current session. |
| FR-05 | Emotion sorting | The user can select and run custom Bubble Sort, Insertion Sort, or Selection Sort on journal entries by emotion. |
| FR-06 | Emotion searching | The user can select Binary Tree, HashMap, or Doubly Linked List search by emotion and view matching entries. |
| FR-07 | Pie chart | The application displays emotion frequencies as a pie chart drawn with Jetpack Compose Canvas. |
| FR-08 | Drag-and-drop delete | The user can remove an entry by dragging it to a visible delete or trash target. |
| FR-09 | Help system | A local HTML help file is packaged with and displayed inside the Android application. |
| FR-10 | REST communication | Android and FastAPI exchange HTTP REST requests and JSON responses. |
| FR-11 | Algorithm selection | Sorting and searching strategies are selectable through clear UI controls such as dropdowns. |
| FR-12 | User feedback | The UI clearly communicates analysis progress, successful results, validation issues, and connection/service errors. |

## Non-functional requirements

| ID | Quality area | Requirement |
|---|---|---|
| NFR-01 | Code quality | Kotlin and Python code follows OOP principles where appropriate, clear naming, consistent formatting, modular structure, and useful comments. |
| NFR-02 | Usability | Core actions are clearly labelled and accessible from the main Compose workflow without unnecessary navigation. |
| NFR-03 | Responsiveness | Backend work does not block the Android main thread, and visible feedback is shown while analysis is pending. |
| NFR-04 | Maintainability | UI, state, networking, models, backend integration, sorting, searching, data structures, and visualisation have focused responsibilities. |
| NFR-05 | Testability | Major features, algorithms, and structures can be tested independently and through end-to-end manual cases. |
| NFR-06 | Compatibility | The final application is tested on the selected assessment emulator and/or physical Android device, with the environment recorded. |
| NFR-07 | Local processing | AI inference runs locally through Ollama rather than a hosted cloud AI service. |
| NFR-08 | Documentation | The project includes accurate setup instructions, screenshots, test evidence, user documentation, technical documentation, and meaningful Git history. |

## Mandatory implementation constraints

- Android code must use Kotlin and Jetpack Compose.
- The backend must use Python FastAPI and run through Uvicorn.
- AI inference must use local Ollama with the `gemma:2b` model.
- Bubble Sort, Insertion Sort, and Selection Sort must be custom implementations; built-in sorting methods cannot replace them.
- Binary Tree search and Doubly Linked List search require custom data structures. HashMap search must remain a distinct, explicit strategy.
- The emotion pie chart must be drawn using Compose Canvas.
- Deletion must use drag-and-drop interaction.
- Help must be supplied as a local HTML file.
- Frontend/backend communication must use REST and JSON.

## Current implemented baseline

The repository currently provides:

- Git is initialised.
- `AGENTS.md`, `README.md`, `.gitignore`, `docs`, `backend`, and `android` exist.
- `android/AIJournalCompanion` contains a Kotlin and Jetpack Compose Android Studio project with a journal input screen, character count, and Analyze button.
- Android input validation rejects blank and whitespace-only entries and entries longer than 5,000 characters after trimming (partial implementation of FR-01 and FR-12).
- The first Android UI increment displays an explicit notice when valid input is submitted because Android networking and analysis result presentation are not implemented yet.
- `backend/.venv` exists and contains FastAPI, Uvicorn, Requests, and Pydantic.
- Backend runtime and test dependency files record the direct Python dependencies.
- `backend/main.py` defines a FastAPI application.
- `GET /` returns a simple status message and does not attempt Ollama integration.
- `POST /analyze` validates journal text and defines the structured REST/JSON response boundary.
- The modular analysis service calls local Ollama through `POST /api/generate` using `gemma:2b`, a constrained prompt, non-streaming output, and the Pydantic JSON schema.
- Automated backend tests verify validation, success, unavailable-service behaviour, invalid model responses, Ollama HTTP communication, and OpenAPI registration.
- Architecture and requirements documents are aligned with the supplied planning document.

## Planned API contract

The analysis endpoint is `POST /analyze`. Its request contains a trimmed, non-empty `text` string with a maximum length of 5,000 characters. Its successful response contains an `emotion` value and an `advice` string of no more than 300 characters.

The controlled emotion set is `JOY`, `SADNESS`, `ANGER`, `FEAR`, `SURPRISE`, and `NEUTRAL`. The set must be used consistently by backend parsing, Android models, sorting, searching, and chart categories.

Ollama connection, timeout, service, and missing-model failures return HTTP `503`. Generated content that cannot be validated against the response schema returns HTTP `502`. Request validation failures use FastAPI's HTTP `422` response.

`GET /` remains a lightweight backend status operation.

## Key acceptance and test considerations

- Verify empty and whitespace-only journal input behaviour.
- Verify loading, success, invalid-response, connection, FastAPI, and Ollama error states.
- Verify every sorting algorithm with empty, single-item, duplicate-emotion, ordered, and reverse-ordered histories.
- Verify every search strategy returns all appropriate duplicate-emotion matches and reports no-match results clearly.
- Verify pie-chart counts, total angle, labels/legend, empty state, and updates after deletion.
- Verify drag cancellation, successful trash-target deletion, state refresh, and accidental-drop behaviour.
- Verify help loads without network access and matches the implemented controls and in-memory limitation.
- Verify REST request and response JSON independently through FastAPI before Android integration.
- Record actual commands, devices, procedures, results, defects, and retests; do not infer a passing result from code inspection alone.

## Documentation and change control

Requirements changes must be assessed for impact on scope, schedule, tests, help, README, and architecture. Assessment-critical or scope-changing items should be confirmed with the lecturer or client representative where necessary. After an approved change, update the relevant implementation and documentation together and preserve the change in Git history.

The controlled emotion set, `/analyze` endpoint name, and GitHub hosting have been established. Remaining open items include confirming the primary test device or emulator, exact assessment due date, and any assessor clarification about the local HTML help presentation.
