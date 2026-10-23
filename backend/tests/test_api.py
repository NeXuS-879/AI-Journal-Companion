"""Contract tests for the FastAPI application."""

from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from backend.main import app
from backend.models import AnalyzeResponse, Emotion
from backend.services import (
    AnalysisService,
    AnalysisUnavailableError,
    InvalidAnalysisResponseError,
    get_analysis_service,
)


class StubAnalysisService(AnalysisService):
    """Deterministic service used only to verify the HTTP contract."""

    def __init__(self) -> None:
        self.received_text: str | None = None

    def analyze(self, text: str) -> AnalyzeResponse:
        self.received_text = text
        return AnalyzeResponse(
            emotion=Emotion.JOY,
            advice="Keep building on what went well today.",
        )


class UnavailableAnalysisService(AnalysisService):
    def analyze(self, text: str) -> AnalyzeResponse:
        del text
        raise AnalysisUnavailableError("Cannot connect to Ollama.")


class InvalidResponseAnalysisService(AnalysisService):
    def analyze(self, text: str) -> AnalyzeResponse:
        del text
        raise InvalidAnalysisResponseError("Ollama returned invalid analysis.")


@pytest.fixture
def client() -> Iterator[TestClient]:
    """Create an isolated API client and clear dependency overrides."""

    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


def test_status_endpoint(client: TestClient) -> None:
    response = client.get("/")

    assert response.status_code == 200
    assert response.json() == {
        "status": "AI Journal Companion backend is running"
    }


def test_analyze_rejects_whitespace_only_text(client: TestClient) -> None:
    response = client.post("/analyze", json={"text": "   "})

    assert response.status_code == 422


def test_analyze_reports_unavailable_ollama(client: TestClient) -> None:
    app.dependency_overrides[get_analysis_service] = UnavailableAnalysisService

    response = client.post("/analyze", json={"text": "Today was productive."})

    assert response.status_code == 503
    assert response.json() == {"detail": "Cannot connect to Ollama."}


def test_analyze_reports_invalid_ollama_response(client: TestClient) -> None:
    app.dependency_overrides[get_analysis_service] = InvalidResponseAnalysisService

    response = client.post("/analyze", json={"text": "Today was productive."})

    assert response.status_code == 502
    assert response.json() == {"detail": "Ollama returned invalid analysis."}


def test_analyze_returns_structured_response_and_trimmed_text(
    client: TestClient,
) -> None:
    stub_service = StubAnalysisService()
    app.dependency_overrides[get_analysis_service] = lambda: stub_service

    response = client.post(
        "/analyze",
        json={"text": "  I completed an important task today.  "},
    )

    assert response.status_code == 200
    assert response.json() == {
        "emotion": "JOY",
        "advice": "Keep building on what went well today.",
    }
    assert stub_service.received_text == "I completed an important task today."


def test_openapi_includes_analyze_endpoint(client: TestClient) -> None:
    response = client.get("/openapi.json")

    assert response.status_code == 200
    assert "/analyze" in response.json()["paths"]
