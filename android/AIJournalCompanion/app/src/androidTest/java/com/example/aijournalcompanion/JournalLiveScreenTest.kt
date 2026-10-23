package com.example.aijournalcompanion

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Optional complete screen-to-Ollama check, enabled by the liveBackend runner flag. */
class JournalLiveScreenTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun submitEntryAndRetainResultAcrossActivityRecreation() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("liveBackend") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= 37) {
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName, Manifest.permission.ACCESS_LOCAL_NETWORK,
            )
        }
        val entry = "I completed an important assignment today and feel proud."
        rule.onNodeWithText("Journal entry").performTextReplacement(entry)
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText(entry).assertIsDisplayed()
        rule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        rule.waitUntil(150_000) {
            rule.onAllNodesWithText("Emotion analysis").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Emotion analysis").performScrollTo().assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Emotion analysis").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("history_tab").performClick()
        rule.onNodeWithTag("journal_feed").performScrollToNode(hasText("1 entry"))
        rule.onNodeWithText("1 entry").assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Journal history").assertIsDisplayed()
        rule.onNodeWithTag("journal_tab").performClick()
        // A second successful request adds another entry; recreation must retain both.
        rule.onNodeWithText("Journal entry").performScrollTo().performTextReplacement("Today was calm and peaceful.")
        rule.onNodeWithText("Analyse entry").performScrollTo().performClick()
        rule.waitUntil(150_000) {
            rule.onAllNodesWithText("Analyzing…").fetchSemanticsNodes().isEmpty()
        }
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("history_tab").performClick()
        rule.onNodeWithTag("journal_feed").performScrollToNode(hasText("2 entries"))
        rule.onNodeWithText("2 entries").assertIsDisplayed()
    }
}
