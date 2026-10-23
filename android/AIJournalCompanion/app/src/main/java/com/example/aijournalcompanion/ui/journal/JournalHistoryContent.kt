package com.example.aijournalcompanion.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.aijournalcompanion.R
import com.example.aijournalcompanion.model.JournalEntry

/** Layout slots only: no search, sort, chart, help or deletion behaviour is installed. */
@Composable
internal fun JournalHistoryContent(
    history: List<JournalEntry>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.weight(1f).testTag("journal_feed"),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "future_controls") { FutureHistoryControls() }
            item(key = "history_header") {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(pluralStringResource(R.plurals.history_count, history.size, history.size),
                            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = {}, enabled = false,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("future_chart")) {
                            Text(stringResource(R.string.show_chart))
                        }
                    }
                    Text(stringResource(R.string.history_session_notice), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (history.isEmpty()) {
                item(key = "history_empty") { Text(stringResource(R.string.history_empty)) }
            }
            // Full existing content remains readable; detail popups and drag handles are deferred.
            items(history, key = { it.id }) { entry -> JournalEntryCard(entry) }
        }
        Surface(color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedButton(onClick = {}, enabled = false,
                modifier = Modifier.heightIn(min = 48.dp).testTag("future_delete")) {
                Text(stringResource(R.string.delete_future))
            }
        }
    }
}

@Composable
private fun FutureHistoryControls() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.future_controls_notice), style = MaterialTheme.typography.bodySmall)
        FutureSelector(stringResource(R.string.emotion_filter), stringResource(R.string.all_emotions), Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FutureSelector(stringResource(R.string.search_method), "Binary Tree", Modifier.weight(1f))
            Button(onClick = {}, enabled = false, modifier = Modifier.heightIn(min = 56.dp).testTag("future_search")) {
                Text(stringResource(R.string.search))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FutureSelector(stringResource(R.string.sort_method), "Bubble Sort", Modifier.weight(1f))
            Button(onClick = {}, enabled = false, modifier = Modifier.heightIn(min = 56.dp).testTag("future_sort")) {
                Text(stringResource(R.string.sort))
            }
        }
    }
}

@Composable
private fun FutureSelector(label: String, value: String, modifier: Modifier) {
    OutlinedTextField(
        value = value, onValueChange = {}, enabled = false, readOnly = true,
        label = { Text(label) }, trailingIcon = { Text("▾") }, modifier = modifier,
    )
}
