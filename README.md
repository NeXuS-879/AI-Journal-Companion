# AI Journal Companion

AI Journal Companion is an OOP 3 assessment project for an Android journaling application. The planned application accepts a journal entry, sends it to a local Python FastAPI backend, and will later use Ollama with `gemma:2b` to return a dominant emotion and one line of advice.

This repository currently contains the planning material, project rules, architecture and requirements documentation, and a minimal runnable backend. The Android Studio project and Ollama integration have intentionally not been created yet.

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
|   |-- main.py
|   `-- requirements.txt
`-- android/
    `-- .gitkeep
```

## Backend setup

From the repository root on Windows PowerShell:

```powershell
.\backend\.venv\Scripts\Activate.ps1
python -m pip install -r .\backend\requirements.txt
uvicorn backend.main:app --reload
```

Alternatively, run from `backend`:

```powershell
Set-Location backend
.\.venv\Scripts\Activate.ps1
uvicorn main:app --reload
```

Open `http://127.0.0.1:8000/` to check the status route or `http://127.0.0.1:8000/docs` for FastAPI's generated API interface.

Expected status response:

```json
{"status":"AI Journal Companion backend is running"}
```

## Current scope

- Repository and Git setup
- Python virtual environment and backend dependencies
- Minimal FastAPI application with `GET /`
- Architecture and requirements documentation based on the supplied planning document

Not yet implemented:

- Android Studio Kotlin and Jetpack Compose project
- Journal entry UI and in-memory history
- `POST /analyze`
- Ollama and `gemma:2b` integration
- Required sorting, searching, data structures, chart, drag-and-drop, and local HTML help

## Assessment constraints

The required technology and features are recorded in [AGENTS.md](AGENTS.md). In particular, the custom Bubble Sort, Insertion Sort, Selection Sort, Binary Tree search, HashMap search, and Doubly Linked List search must remain visible, selectable implementations rather than being replaced by convenience library methods.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Requirements](docs/REQUIREMENTS.md)
- Planning source: `docs/CITEMS-APP-001_v1.0_AI_Journal_Companion_Planning.docx`

