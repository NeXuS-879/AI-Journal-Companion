package com.example.aijournalcompanion

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
        rule.onNodeWithText("Analyze").performScrollTo().performClick()
        rule.waitUntil(150_000) {
            rule.onAllNodesWithText("Your reflection").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Your reflection").performScrollTo().assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Your reflection").performScrollTo().assertIsDisplayed()
    }
}
