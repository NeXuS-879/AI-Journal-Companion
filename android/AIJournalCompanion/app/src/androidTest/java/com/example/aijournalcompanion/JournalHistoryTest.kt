package com.example.aijournalcompanion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.example.aijournalcompanion.model.JournalEntry
import com.example.aijournalcompanion.network.model.Emotion
import com.example.aijournalcompanion.ui.journal.JournalScreen
import com.example.aijournalcompanion.ui.journal.JournalUiState
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme
import org.junit.Rule
import org.junit.Test

class JournalHistoryTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun emptyHistoryShowsSessionLimitation() {
        rule.setContent { AIJournalCompanionTheme { JournalScreen(JournalUiState(), {}, {}) } }
        rule.onNodeWithTag("journal_feed")
            .performScrollToNode(hasText("Your analyzed entries will appear here."))
        rule.onNodeWithText("Journal history (0)").assertIsDisplayed()
        rule.onNodeWithText("History is kept in memory for this session.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun historyCardsShowEntryEmotionAdviceAndCanScrollToOlderEntries() {
        val entries = (30 downTo 1).map { index ->
            JournalEntry(
                text = "Journal entry $index",
                emotion = Emotion.JOY,
                advice = "Advice for entry $index.",
                id = "entry-$index",
            )
        }
        rule.setContent {
            AIJournalCompanionTheme {
                JournalScreen(JournalUiState(history = entries), {}, {})
            }
        }
        rule.onNodeWithTag("journal_feed")
            .performScrollToNode(hasTestTag("journal_entry_entry-30"))
        rule.onNodeWithText("Journal entry 30").assertIsDisplayed()
        rule.onNodeWithText("Advice: Advice for entry 30.").assertIsDisplayed()
        rule.onAllNodesWithText("Emotion: 😊 JOY").onFirst().assertIsDisplayed()
        rule.onNodeWithTag("journal_feed")
            .performScrollToNode(hasTestTag("journal_entry_entry-1"))
        rule.onNodeWithText("Journal entry 1").assertIsDisplayed()
        rule.onNodeWithText("Advice: Advice for entry 1.").assertIsDisplayed()
    }
}
