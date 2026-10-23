package com.example.aijournalcompanion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.aijournalcompanion.ui.journal.JournalScreen
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class JournalScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun showScreen() {
        composeRule.setContent {
            AIJournalCompanionTheme { JournalScreen() }
        }
    }

    @Test
    fun blankSubmissionShowsValidationError() {
        composeRule.onNodeWithText("Analyze").performClick()
        composeRule.onNodeWithText("Please write a journal entry before analyzing.")
            .assertIsDisplayed()
    }

    @Test
    fun whitespaceSubmissionIsRejectedAndEditingClearsError() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("   ")
        composeRule.onNodeWithText("Analyze").performScrollTo().performClick()
        composeRule.onNodeWithText("Please write a journal entry before analyzing.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Journal entry").performTextReplacement("I feel proud.")
        composeRule.onNodeWithText("Please write a journal entry before analyzing.")
            .assertDoesNotExist()
    }

    @Test
    fun validSubmissionExplainsThatAnalysisIsNotConnected() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("I feel proud.")
        composeRule.onNodeWithText("Analyze").performScrollTo().performClick()
        composeRule.onNodeWithText(
            "Analysis is not available in this app version yet. Your entry has not been sent.",
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun oversizedEntryIsRejected() {
        composeRule.onNodeWithText("Journal entry").performTextReplacement("a".repeat(5_001))
        composeRule.onNodeWithText("Analyze").performScrollTo().performClick()
        composeRule.onNodeWithText("Keep your entry to 5000 characters or fewer.")
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(
            "Analysis is not available in this app version yet. Your entry has not been sent.",
        ).assertDoesNotExist()
    }
}
