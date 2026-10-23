package com.example.aijournalcompanion.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aijournalcompanion.R
import com.example.aijournalcompanion.model.JournalEntry
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Reusable history item; a stable ID will also support later drag-to-delete. */
@Composable
fun JournalEntryCard(entry: JournalEntry) {
    val timestamp = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault()).format(entry.createdAt)
    Card(modifier = Modifier.fillMaxWidth().testTag("journal_entry_${entry.id}")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(timestamp, style = MaterialTheme.typography.labelMedium)
            Text(entry.text, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.history_emotion, entry.emotion.emoji(), entry.emotion.name),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.history_advice, entry.advice),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
