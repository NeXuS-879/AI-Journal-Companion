"""Analysis service abstractions used by the FastAPI routes."""

from abc import ABC, abstractmethod
import os

from backend.models import AnalyzeResponse
from backend.ollama_client import (
    DEFAULT_OLLAMA_BASE_URL,
    DEFAULT_OLLAMA_MODEL,
    OllamaClient,
    OllamaInvalidResponseError,
    OllamaUnavailableError,
)


class AnalysisUnavailableError(RuntimeError):
    """Raised when the journal analysis provider is unavailable."""


class InvalidAnalysisResponseError(RuntimeError):
    """Raised when an analysis provider returns invalid structured data."""


class AnalysisService(ABC):
    """Contract for current and future journal analysis services."""

    @abstractmethod
    def analyze(self, text: str) -> AnalyzeResponse:
        """Analyze journal text and return a structured result."""


class OllamaAnalysisService(AnalysisService):
    """Analyse journal text through the local Ollama client."""

    def __init__(self, client: OllamaClient) -> None:
        self._client = client

    def analyze(self, text: str) -> AnalyzeResponse:
        try:
            return self._client.analyze(text)
        except OllamaUnavailableError as exc:
            raise AnalysisUnavailableError(str(exc)) from exc
        except OllamaInvalidResponseError as exc:
            raise InvalidAnalysisResponseError(str(exc)) from exc


def get_analysis_service() -> AnalysisService:
    """Provide the active analysis service for FastAPI dependency injection."""

    client = OllamaClient(
        base_url=os.getenv("OLLAMA_BASE_URL", DEFAULT_OLLAMA_BASE_URL),
        model=os.getenv("OLLAMA_MODEL", DEFAULT_OLLAMA_MODEL),
    )
    return OllamaAnalysisService(client)
