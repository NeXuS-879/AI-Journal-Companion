package com.example.aijournalcompanion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.onNodeWithTag
import com.example.aijournalcompanion.ui.journal.JournalScreen
import com.example.aijournalcompanion.ui.journal.JournalViewModel
import com.example.aijournalcompanion.network.JournalApi
import com.example.aijournalcompanion.network.ApiException
import java.io.IOException
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.AnalyzeResponse
import com.example.aijournalcompanion.network.model.Emotion
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class JournalScreenTest {
    private val response = CompletableDeferred<AnalyzeResponse>()
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun showScreen() {
        val model = JournalViewModel(object : JournalApi {
            override suspend fun analyze(request: AnalyzeRequest): AnalyzeResponse = response.await()
        })
        composeRule.setContent {
            val state by model.uiState.collectAsState()
            AIJournalCompanionTheme {
                JournalScreen(state, model::onInputChanged, { model.requestAnalysis(true) })
            }
        }
    }

    @Test
    fun blankSubmissionShowsValidationError() {
        composeRule.onNodeWithText("Analyse entry").performClick()
        composeRule.onNodeWithText("Please write a journal entry before analyzing.")
            .assertIsDisplayed()
    }

    @Test
    fun whitespaceSubmissionIsRejectedAndEditingClearsError() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("   ")
        composeRule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        composeRule.onNodeWithText("Please write a journal entry before analyzing.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Journal entry").performTextReplacement("I feel proud.")
        composeRule.onNodeWithText("Please write a journal entry before analyzing.")
            .assertDoesNotExist()
    }

    @Test
    fun validSubmissionShowsLoadingThenEmojiAndAdvice() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("I feel proud.")
        composeRule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        composeRule.onNodeWithText("Analyzing…").assertIsNotEnabled()
        composeRule.onNodeWithText("Journal entry").assertIsNotEnabled()
        composeRule.onNodeWithText("Reflecting on your entry. This may take a moment.")
            .performScrollTo().assertIsDisplayed()
        composeRule.runOnIdle {
            response.complete(AnalyzeResponse(Emotion.JOY, "Celebrate your progress."))
        }
        composeRule.onNodeWithText("😊 JOY").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Celebrate your progress.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun oversizedEntryIsRejected() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("a".repeat(5_001))
        composeRule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        composeRule.onNodeWithText("Keep your entry to 5000 characters or fewer.")
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Emotion analysis").assertDoesNotExist()
    }

    @Test
    fun connectionErrorKeepsDraftAndEnablesRetry() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("I feel proud.")
        composeRule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        composeRule.runOnIdle { response.completeExceptionally(ApiException.Connection(IOException())) }
        composeRule.onNodeWithText("Cannot reach the backend.", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("I feel proud.").assertExists()
        composeRule.onNodeWithText("Analyse entry").assertIsEnabled()
    }

    @Test
    fun navigationPreservesDraftAndLatestSuccessfulEntry() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("I feel proud.")
        composeRule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        composeRule.runOnIdle { response.complete(AnalyzeResponse(Emotion.JOY, "Celebrate your progress.")) }
        composeRule.onNodeWithText("Celebrate your progress.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Journal entry").performScrollTo().performTextReplacement("My next draft.")
        composeRule.onNodeWithTag("history_tab").performClick()
        composeRule.onNodeWithText("1 entry").assertIsDisplayed()
        composeRule.onNodeWithTag("journal_tab").performClick()
        composeRule.onNodeWithText("My next draft.").assertExists()
        composeRule.onNodeWithText("Celebrate your progress.").performScrollTo().assertIsDisplayed()
    }
}
