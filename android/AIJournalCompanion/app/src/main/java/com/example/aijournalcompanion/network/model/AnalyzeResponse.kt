package com.example.aijournalcompanion.network.model

/** Successful response DTO. Parsing normalises advice before construction. */
data class AnalyzeResponse(val emotion: Emotion, val advice: String) {
    init {
        require(advice.isNotBlank()) { "Advice must not be blank." }
        require(advice.codePointCount(0, advice.length) <= 300) {
            "Advice must not exceed 300 characters."
        }
        require('\n' !in advice && '\r' !in advice) { "Advice must be one line." }
    }
}
