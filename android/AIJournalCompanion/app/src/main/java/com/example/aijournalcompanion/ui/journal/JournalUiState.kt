package com.example.aijournalcompanion.ui.journal

import com.example.aijournalcompanion.network.model.AnalyzeResponse
import com.example.aijournalcompanion.model.JournalEntry

/** Immutable state retained in memory by JournalViewModel across rotation. */
data class JournalUiState(
    val input: String = "",
    val inputError: JournalInputError? = null,
    val isLoading: Boolean = false,
    val awaitingPermission: Boolean = false,
    val result: AnalyzeResponse? = null,
    val error: JournalAnalysisError? = null,
    val history: List<JournalEntry> = emptyList(),
) {
    val characterCount: Int
        get() = input.trim().let { it.codePointCount(0, it.length) }
    val isBusy: Boolean
        get() = isLoading || awaitingPermission
}

enum class JournalInputError { EMPTY, TOO_LONG }
enum class JournalAnalysisError {
    CONNECTION, TIMEOUT, PERMISSION, INVALID_ENTRY, SERVICE_UNAVAILABLE,
    INVALID_RESPONSE, SERVER, UNEXPECTED,
}
