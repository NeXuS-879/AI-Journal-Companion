package com.example.aijournalcompanion.network

import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.AnalyzeResponse
import com.example.aijournalcompanion.network.model.Emotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiModelsTest {
    @Test
    fun rejectsBlankAndOversizedRequests() {
        listOf("", " \n\t ", "a".repeat(5_001)).forEach { text ->
            assertThrows(IllegalArgumentException::class.java) { AnalyzeRequest(text) }
        }
    }

    @Test
    fun acceptsBoundaryAndCountsUnicodeCodePointsLikePython() {
        AnalyzeRequest("  ${"a".repeat(5_000)}  ")
        AnalyzeRequest("\uD83D\uDE0A".repeat(5_000))
    }

    @Test
    fun rejectsBlankMultilineAndOversizedAdvice() {
        listOf("", "   ", "One.\nTwo.", "One.\rTwo.", "a".repeat(301)).forEach { advice ->
            assertThrows(IllegalArgumentException::class.java) {
                AnalyzeResponse(Emotion.JOY, advice)
            }
        }
    }

    @Test
    fun emotionLabelsMatchBackendContract() {
        assertEquals(
            listOf("JOY", "SADNESS", "ANGER", "FEAR", "SURPRISE", "NEUTRAL"),
            Emotion.entries.map { it.name },
        )
    }
}
