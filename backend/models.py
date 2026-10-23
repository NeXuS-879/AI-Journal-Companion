"""Validated API models for journal analysis."""

from enum import Enum

from pydantic import BaseModel, ConfigDict, Field, field_validator


class Emotion(str, Enum):
    """Controlled emotion labels used across the API and Android app."""

    JOY = "JOY"
    SADNESS = "SADNESS"
    ANGER = "ANGER"
    FEAR = "FEAR"
    SURPRISE = "SURPRISE"
    NEUTRAL = "NEUTRAL"


class AnalyzeRequest(BaseModel):
    """Journal text submitted for analysis."""

    model_config = ConfigDict(extra="forbid")

    text: str = Field(min_length=1, max_length=5_000)

    @field_validator("text", mode="before")
    @classmethod
    def strip_text(cls, value: object) -> object:
        """Trim surrounding whitespace before length validation."""

        if isinstance(value, str):
            return value.strip()
        return value


class AnalyzeResponse(BaseModel):
    """Structured emotion and advice returned to the Android app."""

    model_config = ConfigDict(extra="forbid")

    emotion: Emotion
    advice: str = Field(
        min_length=1,
        max_length=300,
        pattern=r"^[^\r\n]+$",
    )

    @field_validator("advice", mode="before")
    @classmethod
    def strip_advice(cls, value: object) -> object:
        """Trim model-generated advice before length validation."""

        if isinstance(value, str):
            return value.strip()
        return value
