package com.example.aijournalcompanion.network

import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.AnalyzeResponse

/** Injectable boundary for the future journal ViewModel and its tests. */
interface JournalApi {
    /** Returns a validated response or throws an ApiException; cancellation propagates. */
    suspend fun analyze(request: AnalyzeRequest): AnalyzeResponse
}
