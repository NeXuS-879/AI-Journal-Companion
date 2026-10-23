package com.example.aijournalcompanion.model

import com.example.aijournalcompanion.network.model.Emotion
import java.time.Instant
import java.util.UUID

/** One successfully analysed entry in the current app session, independent of HTTP. */
data class JournalEntry(
    val text: String,
    val emotion: Emotion,
    val advice: String,
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Instant = Instant.now(),
)
