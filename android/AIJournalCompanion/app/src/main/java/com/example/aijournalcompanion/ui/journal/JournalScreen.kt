package com.example.aijournalcompanion.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aijournalcompanion.R
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme

private const val MAX_JOURNAL_LENGTH = 5_000

/** Entry screen owns temporary input; a later increment will connect analysis. */
@Composable
fun JournalScreen() {
    var journalText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var showAnalysisNotice by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val trimmedText = journalText.trim()
    val errorMessage = when {
        submitted && trimmedText.isEmpty() -> stringResource(R.string.journal_empty_error)
        trimmedText.length > MAX_JOURNAL_LENGTH -> stringResource(
            R.string.journal_length_error,
            MAX_JOURNAL_LENGTH,
        )
        else -> null
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        JournalContent(
            text = journalText,
            errorMessage = errorMessage,
            showAnalysisNotice = showAnalysisNotice,
            onTextChanged = {
                journalText = it
                showAnalysisNotice = false
            },
            onAnalyze = {
                submitted = true
                if (trimmedText.isNotEmpty() && trimmedText.length <= MAX_JOURNAL_LENGTH) {
                    keyboard?.hide()
                    showAnalysisNotice = true
                }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

/** Stateless form keeps rendering separate from input and action handling. */
@Composable
private fun JournalContent(
    text: String,
    errorMessage: String?,
    showAnalysisNotice: Boolean,
    onTextChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.journal_intro),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            label = { Text(stringResource(R.string.journal_label)) },
            placeholder = { Text(stringResource(R.string.journal_placeholder)) },
            supportingText = {
                Text(
                    errorMessage ?: stringResource(
                        R.string.journal_character_count,
                        text.trim().length,
                        MAX_JOURNAL_LENGTH,
                    ),
                )
            },
            isError = errorMessage != null,
            minLines = 6,
            maxLines = 10,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onAnalyze, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.analyze))
        }
        if (showAnalysisNotice) {
            Text(
                text = stringResource(R.string.analysis_not_connected),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JournalScreenPreview() {
    AIJournalCompanionTheme { JournalScreen() }
}
