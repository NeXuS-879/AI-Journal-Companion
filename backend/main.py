"""FastAPI entry point for the AI Journal Companion backend."""

from fastapi import Depends, FastAPI, HTTPException, status

from backend.models import AnalyzeRequest, AnalyzeResponse
from backend.services import (
    AnalysisService,
    AnalysisUnavailableError,
    InvalidAnalysisResponseError,
    get_analysis_service,
)


app = FastAPI(
    title="AI Journal Companion API",
    description="Local backend for the AI Journal Companion OOP 3 assessment.",
    version="0.1.0",
)


@app.get("/")
def read_status() -> dict[str, str]:
    """Return a basic service status without calling Ollama."""

    return {"status": "AI Journal Companion backend is running"}


@app.post(
    "/analyze",
    response_model=AnalyzeResponse,
    responses={
        status.HTTP_502_BAD_GATEWAY: {
            "description": "Ollama returned an invalid analysis response."
        },
        status.HTTP_503_SERVICE_UNAVAILABLE: {
            "description": "The local Ollama service or model is unavailable."
        }
    },
)
def analyze_journal(
    request: AnalyzeRequest,
    service: AnalysisService = Depends(get_analysis_service),
) -> AnalyzeResponse:
    """Validate journal text and delegate it to the configured service."""

    try:
        return service.analyze(request.text)
    except AnalysisUnavailableError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=str(exc),
        ) from exc
    except InvalidAnalysisResponseError as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=str(exc),
        ) from exc
