"""FastAPI entry point for the AI Journal Companion backend."""

from fastapi import FastAPI


app = FastAPI(
    title="AI Journal Companion API",
    description="Local backend for the AI Journal Companion OOP 3 assessment.",
    version="0.1.0",
)


@app.get("/")
def read_status() -> dict[str, str]:
    """Return a basic service status without calling Ollama."""

    return {"status": "AI Journal Companion backend is running"}

