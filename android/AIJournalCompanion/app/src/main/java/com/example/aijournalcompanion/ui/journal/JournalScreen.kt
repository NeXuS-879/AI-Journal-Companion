package com.example.aijournalcompanion.ui.journal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aijournalcompanion.R
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

/** Only navigation is local UI state; draft, requests and history stay in the ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    state: JournalUiState,
    onInputChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val keyboard = LocalSoftwareKeyboardController.current
    val journalScroll = rememberScrollState()
    val historyScroll = rememberLazyListState()
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (selectedTab == 0) R.string.app_name else R.string.history_screen_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    TextButton(onClick = {}, enabled = false, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.help_future))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0, onClick = { keyboard?.hide(); selectedTab = 0 },
                    icon = { Text("✎") }, label = { Text(stringResource(R.string.journal_tab)) },
                    modifier = Modifier.testTag("journal_tab"),
                )
                NavigationBarItem(
                    selected = selectedTab == 1, onClick = { keyboard?.hide(); selectedTab = 1 },
                    icon = { Text("☷") }, label = { Text(stringResource(R.string.history_tab)) },
                    modifier = Modifier.testTag("history_tab"),
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding).fillMaxSize()
        if (selectedTab == 0) {
            JournalContent(state, onInputChanged, onAnalyze, journalScroll, contentModifier)
        } else {
            JournalHistoryContent(state.history, historyScroll, contentModifier)
        }
    }
}

internal fun JournalAnalysisError.messageResource(): Int = when (this) {
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
internal fun Emotion.emoji(): String = when (this) {
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
