package com.example.aijournalcompanion.ui.journal

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aijournalcompanion.R
import com.example.aijournalcompanion.network.model.AnalyzeRequest

/** Input and the last successful analysis are independent, so editing never hides history. */
@Composable
internal fun JournalContent(
    state: JournalUiState,
    onInputChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val inputError = when (state.inputError) {
        JournalInputError.EMPTY -> stringResource(R.string.journal_empty_error)
        JournalInputError.TOO_LONG -> stringResource(R.string.journal_length_error, AnalyzeRequest.MAX_TEXT_LENGTH)
        null -> null
    }
    Column(
        modifier = modifier.testTag("journal_form_scroll").verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.journal_intro), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = state.input, onValueChange = onInputChanged, enabled = !state.isBusy,
            label = { Text(stringResource(R.string.journal_label)) },
            placeholder = { Text(stringResource(R.string.journal_placeholder)) },
            supportingText = {
                Text(inputError ?: stringResource(
                    R.string.journal_character_count, state.characterCount, AnalyzeRequest.MAX_TEXT_LENGTH,
                ))
            },
            isError = inputError != null, minLines = 4, maxLines = 8,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onAnalyze, enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(if (state.isLoading) R.string.analyzing else R.string.analyze))
        }
        if (state.isLoading) {
            CircularProgressIndicator()
            Text(stringResource(R.string.analysis_loading), modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            })
        }
        state.error?.let { error ->
            Text(stringResource(error.messageResource()), color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        Text(stringResource(R.string.analysis_result), style = MaterialTheme.typography.titleMedium)
        val latest = state.history.firstOrNull()
        if (latest == null) {
            Text(stringResource(R.string.latest_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.latest_author, latest.formattedTimestamp()),
                        style = MaterialTheme.typography.labelMedium)
                    Text(latest.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${latest.emotion.emoji()} ${latest.emotion.name}", style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.emotion_analysis), style = MaterialTheme.typography.titleSmall)
                    Text(latest.advice, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
