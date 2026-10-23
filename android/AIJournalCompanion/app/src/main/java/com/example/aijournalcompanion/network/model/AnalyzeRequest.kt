package com.example.aijournalcompanion.network.model

/** Validated request DTO; surrounding whitespace is removed before transmission. */
data class AnalyzeRequest(val text: String) {
    init {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Journal text must not be blank." }
        require(trimmed.codePointCount(0, trimmed.length) <= MAX_TEXT_LENGTH) {
            "Journal text must not exceed $MAX_TEXT_LENGTH characters."
        }
    }

    companion object {
        const val MAX_TEXT_LENGTH = 5_000
    }
}
