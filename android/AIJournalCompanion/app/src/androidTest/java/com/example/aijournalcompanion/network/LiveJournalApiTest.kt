package com.example.aijournalcompanion.network

import android.Manifest
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Explicitly opt in: requires the host FastAPI and local gemma:2b services. */
@RunWith(AndroidJUnit4::class)
class LiveJournalApiTest {
    @Test
    fun analyzesThroughRealFastApiAndOllama() = runBlocking {
        assumeTrue(
            "Live backend checks require -Pandroid.testInstrumentationRunnerArguments.liveBackend=true",
            InstrumentationRegistry.getArguments().getString("liveBackend") == "true",
        )
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 37) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.ACCESS_LOCAL_NETWORK,
            )
        }
        val result = HttpJournalApi(context).analyze(
            AnalyzeRequest("I completed an important assignment today and feel proud."),
        )
        // Wording and the selected valid label may vary; never assert LLM determinism.
        assertTrue(result.advice.isNotBlank())
        assertTrue(result.advice.length <= 300)
    }
}
