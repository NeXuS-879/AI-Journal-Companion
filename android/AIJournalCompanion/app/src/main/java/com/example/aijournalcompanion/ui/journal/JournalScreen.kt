package com.example.aijournalcompanion.ui.journal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aijournalcompanion.R
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.Emotion
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme

/** Lifecycle-aware bridge between Android permission callbacks and ViewModel events. */
@Composable
fun JournalRoute(viewModel: JournalViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(), viewModel::onPermissionResult,
    )
    JournalScreen(
        state = state,
        onInputChanged = viewModel::onInputChanged,
        onAnalyze = {
            keyboard?.hide()
            val hasPermission = Build.VERSION.SDK_INT < 37 || context.checkSelfPermission(
                Manifest.permission.ACCESS_LOCAL_NETWORK,
            ) == PackageManager.PERMISSION_GRANTED
            if (viewModel.requestAnalysis(hasPermission)) {
                permissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        },
    )
}

/** Stateless rendering: input, progress, results and errors come from the ViewModel. */
@Composable
fun JournalScreen(
    state: JournalUiState,
    onInputChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
) {
    val inputError = when (state.inputError) {
        JournalInputError.EMPTY -> stringResource(R.string.journal_empty_error)
        JournalInputError.TOO_LONG -> stringResource(
            R.string.journal_length_error, AnalyzeRequest.MAX_TEXT_LENGTH,
        )
        null -> null
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.journal_intro), style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChanged,
                enabled = !state.isBusy,
                label = { Text(stringResource(R.string.journal_label)) },
                placeholder = { Text(stringResource(R.string.journal_placeholder)) },
                supportingText = {
                    Text(inputError ?: stringResource(
                        R.string.journal_character_count,
                        state.characterCount, AnalyzeRequest.MAX_TEXT_LENGTH,
                    ))
                },
                isError = inputError != null,
                minLines = 6,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onAnalyze, enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (state.isLoading) R.string.analyzing else R.string.analyze))
            }
            if (state.isLoading) {
                CircularProgressIndicator()
                Text(
                    stringResource(R.string.analysis_loading),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            state.error?.let { error ->
                Text(
                    stringResource(error.messageResource()),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            state.result?.let { result ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                ) {
                    Text(stringResource(R.string.analysis_result), style = MaterialTheme.typography.titleMedium)
                    Text("${result.emotion.emoji()} ${result.emotion.name}", style = MaterialTheme.typography.headlineSmall)
                    Text(result.advice, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

private fun JournalAnalysisError.messageResource(): Int = when (this) {
    JournalAnalysisError.CONNECTION -> R.string.analysis_connection_error
    JournalAnalysisError.TIMEOUT -> R.string.analysis_timeout_error
    JournalAnalysisError.PERMISSION -> R.string.analysis_permission_error
    JournalAnalysisError.INVALID_ENTRY -> R.string.analysis_invalid_entry
    JournalAnalysisError.SERVICE_UNAVAILABLE -> R.string.analysis_unavailable_error
    JournalAnalysisError.INVALID_RESPONSE -> R.string.analysis_response_error
    JournalAnalysisError.SERVER -> R.string.analysis_server_error
    JournalAnalysisError.UNEXPECTED -> R.string.analysis_unexpected_error
}

/** Presentation mapping stays out of the REST enum and JSON client. */
private fun Emotion.emoji(): String = when (this) {
    Emotion.JOY -> "😊"
    Emotion.SADNESS -> "😢"
    Emotion.ANGER -> "😠"
    Emotion.FEAR -> "😨"
    Emotion.SURPRISE -> "😮"
    Emotion.NEUTRAL -> "😐"
}

@Preview(showBackground = true)
@Composable
private fun JournalScreenPreview() {
    AIJournalCompanionTheme { JournalScreen(JournalUiState(), {}, {}) }
}
