"""Unit tests for structured local Ollama communication."""

import json
from unittest.mock import Mock

import pytest
import requests

from backend.models import Emotion
from backend.ollama_client import (
    OllamaClient,
    OllamaInvalidResponseError,
    OllamaUnavailableError,
)


def make_response(payload: object) -> Mock:
    response = Mock(spec=requests.Response)
    response.json.return_value = payload
    response.raise_for_status.return_value = None
    return response


def test_analyze_sends_structured_non_streaming_request() -> None:
    session = Mock(spec=requests.Session)
    session.post.return_value = make_response(
        {
            "response": json.dumps(
                {
                    "emotion": "JOY",
                    "advice": "Celebrate the progress you made today.",
                }
            ),
            "done": True,
        }
    )
    client = OllamaClient(session=session, timeout_seconds=12.0)

    result = client.analyze("I finished an important assignment.")

    assert result.emotion is Emotion.JOY
    assert result.advice == "Celebrate the progress you made today."
    request_url = session.post.call_args.args[0]
    request_options = session.post.call_args.kwargs
    assert request_url == "http://127.0.0.1:11434/api/generate"
    assert request_options["timeout"] == 12.0
    assert request_options["json"]["model"] == "gemma:2b"
    assert request_options["json"]["stream"] is False
    assert request_options["json"]["options"] == {"temperature": 0}
    assert request_options["json"]["format"]["required"] == [
        "emotion",
        "advice",
    ]
    assert "I finished an important assignment." in request_options["json"][
        "prompt"
    ]


def test_analyze_reports_connection_failure() -> None:
    session = Mock(spec=requests.Session)
    session.post.side_effect = requests.ConnectionError("connection refused")
    client = OllamaClient(session=session)

    with pytest.raises(
        OllamaUnavailableError,
        match="Cannot connect to the local Ollama service",
    ):
        client.analyze("A journal entry")


def test_analyze_preserves_ollama_http_error_message() -> None:
    session = Mock(spec=requests.Session)
    response = make_response({"error": "model 'gemma:2b' not found"})
    response.status_code = 404
    error = requests.HTTPError("404")
    error.response = response
    response.raise_for_status.side_effect = error
    session.post.return_value = response
    client = OllamaClient(session=session)

    with pytest.raises(
        OllamaUnavailableError,
        match="model 'gemma:2b' not found",
    ):
        client.analyze("A journal entry")


@pytest.mark.parametrize(
    "payload",
    [
        [],
        {},
        {"response": ""},
        {"response": "not json"},
        {"response": '{"emotion":"CONFUSED","advice":"Pause."}'},
        {"response": '{"emotion":"JOY"}'},
        {
            "response": (
                '{"emotion":"JOY","advice":"First line.\\nSecond line."}'
            )
        },
    ],
)
def test_analyze_rejects_invalid_generated_content(
    payload: object,
) -> None:
    session = Mock(spec=requests.Session)
    session.post.return_value = make_response(payload)
    client = OllamaClient(session=session)

    with pytest.raises(OllamaInvalidResponseError):
        client.analyze("A journal entry")
