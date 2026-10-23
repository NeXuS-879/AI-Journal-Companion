package com.example.aijournalcompanion.ui.journal

import com.example.aijournalcompanion.network.ApiException
import com.example.aijournalcompanion.network.JournalApi
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.AnalyzeResponse
import com.example.aijournalcompanion.network.model.Emotion
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val result = AnalyzeResponse(Emotion.JOY, "Celebrate your progress.")
    private var calls = 0
    private var received: AnalyzeRequest? = null
    private var handler: suspend () -> AnalyzeResponse = { result }
    private lateinit var model: JournalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        model = JournalViewModel(object : JournalApi {
            override suspend fun analyze(request: AnalyzeRequest): AnalyzeResponse {
                calls++
                received = request
                return handler()
            }
        })
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun invalidInputNeverCallsApiOrRequestsPermission() = runTest(dispatcher) {
        listOf("", " \n ", "x".repeat(5_001)).forEach {
            model.onInputChanged(it)
            assertFalse(model.requestAnalysis(false))
            assertNotNull(model.uiState.value.inputError)
        }
        advanceUntilIdle()
        assertEquals(0, calls)
        assertTrue(model.uiState.value.history.isEmpty())
    }

    @Test
    fun loadingPreventsDuplicateCallsAndKeepsSubmittedInputStable() = runTest(dispatcher) {
        val pending = CompletableDeferred<AnalyzeResponse>()
        handler = { pending.await() }
        model.onInputChanged("  I feel proud.  ")
        model.requestAnalysis(true)
        assertTrue(model.uiState.value.isLoading)
        model.requestAnalysis(true)
        model.onInputChanged("Another draft")
        runCurrent()
        assertEquals(1, calls)
        assertEquals("I feel proud.", received?.text)
        assertEquals("  I feel proud.  ", model.uiState.value.input)
        pending.complete(result)
        advanceUntilIdle()
        assertFalse(model.uiState.value.isLoading)
        assertEquals(result, model.uiState.value.result)
        assertEquals(1, model.uiState.value.history.size)
    }

    @Test
    fun permissionGrantResumesSubmissionExactlyOnce() = runTest(dispatcher) {
        model.onInputChanged("I feel proud.")
        assertTrue(model.requestAnalysis(false))
        assertTrue(model.uiState.value.awaitingPermission)
        assertFalse(model.requestAnalysis(false))
        runCurrent()
        assertEquals(0, calls)
        model.onPermissionResult(true)
        model.onPermissionResult(true)
        advanceUntilIdle()
        assertEquals(1, calls)
        assertEquals(result, model.uiState.value.result)
    }

    @Test
    fun permissionDenialKeepsInputAndDoesNotCallApi() = runTest(dispatcher) {
        model.onInputChanged("A journal entry")
        model.requestAnalysis(false)
        model.onPermissionResult(false)
        advanceUntilIdle()
        assertEquals(JournalAnalysisError.PERMISSION, model.uiState.value.error)
        assertFalse(model.uiState.value.isBusy)
        assertEquals("A journal entry", model.uiState.value.input)
        assertEquals(0, calls)
    }

    @Test
    fun failuresAreMappedWithoutDiscardingInputAndRetryCanSucceed() = runTest(dispatcher) {
        val failures = listOf(
            ApiException.Connection(IOException()) to JournalAnalysisError.CONNECTION,
            ApiException.Timeout(IOException()) to JournalAnalysisError.TIMEOUT,
            ApiException.PermissionRequired() to JournalAnalysisError.PERMISSION,
            ApiException.InvalidResponse() to JournalAnalysisError.INVALID_RESPONSE,
            ApiException.Http(422) to JournalAnalysisError.INVALID_ENTRY,
            ApiException.Http(503) to JournalAnalysisError.SERVICE_UNAVAILABLE,
            ApiException.Http(502) to JournalAnalysisError.INVALID_RESPONSE,
            ApiException.Http(500) to JournalAnalysisError.SERVER,
            IllegalStateException() to JournalAnalysisError.UNEXPECTED,
        )
        model.onInputChanged("A journal entry")
        failures.forEach { (exception, expected) ->
            handler = { throw exception }
            model.requestAnalysis(true)
            advanceUntilIdle()
            assertEquals(expected, model.uiState.value.error)
            assertEquals("A journal entry", model.uiState.value.input)
            assertFalse(model.uiState.value.isLoading)
            assertNull(model.uiState.value.result)
            assertTrue(model.uiState.value.history.isEmpty())
        }
        handler = { result }
        model.requestAnalysis(true)
        assertNull(model.uiState.value.error)
        advanceUntilIdle()
        assertEquals(result, model.uiState.value.result)
    }

    @Test
    fun editingAfterSuccessClearsOldResult() = runTest(dispatcher) {
        model.onInputChanged("First entry")
        model.requestAnalysis(true)
        advanceUntilIdle()
        model.onInputChanged("Second entry")
        assertNull(model.uiState.value.result)
        assertEquals("Second entry", model.uiState.value.input)
    }

    @Test
    fun emojiLengthUsesSameCodePointLimitAsApi() {
        model.onInputChanged("😊".repeat(5_000))
        assertEquals(5_000, model.uiState.value.characterCount)
        assertNull(model.uiState.value.inputError)
    }

    @Test
    fun successfulEntriesKeepTextEmotionAdviceAndNewestFirstOrder() = runTest(dispatcher) {
        model.onInputChanged("  First entry  ")
        model.requestAnalysis(true)
        advanceUntilIdle()
        val firstSnapshot = model.uiState.value.history
        val first = firstSnapshot.single()
        assertEquals("First entry", first.text)
        assertEquals(result.emotion, first.emotion)
        assertEquals(result.advice, first.advice)
        assertTrue(first.id.isNotBlank())
        assertNotNull(first.createdAt)

        handler = { AnalyzeResponse(Emotion.SADNESS, "Rest and take care of yourself.") }
        model.onInputChanged("Second entry")
        model.requestAnalysis(true)
        advanceUntilIdle()
        val history = model.uiState.value.history
        assertEquals(listOf("Second entry", "First entry"), history.map { it.text })
        assertEquals(Emotion.SADNESS, history.first().emotion)
        assertEquals(1, firstSnapshot.size)
        assertEquals(first, history.last())
        assertNotEquals(first.id, history.first().id)
    }

    @Test
    fun intentionalRepeatedTextCreatesSeparateEntries() = runTest(dispatcher) {
        model.onInputChanged("A quiet day")
        repeat(2) {
            model.requestAnalysis(true)
            advanceUntilIdle()
        }
        assertEquals(2, model.uiState.value.history.size)
        assertEquals(2, model.uiState.value.history.map { it.id }.toSet().size)
    }

    @Test
    fun failurePermissionDenialAndDraftEditingPreserveExistingHistory() = runTest(dispatcher) {
        model.onInputChanged("Successful entry")
        model.requestAnalysis(true)
        advanceUntilIdle()
        val saved = model.uiState.value.history
        model.onInputChanged("Next draft")
        assertEquals(saved, model.uiState.value.history)
        model.requestAnalysis(false)
        model.onPermissionResult(false)
        assertEquals(saved, model.uiState.value.history)
        handler = { throw ApiException.Connection(IOException()) }
        model.requestAnalysis(true)
        advanceUntilIdle()
        assertEquals(saved, model.uiState.value.history)
        assertEquals("Next draft", model.uiState.value.input)
    }

    @Test
    fun newViewModelStartsWithEmptyHistory() = runTest(dispatcher) {
        model.onInputChanged("Successful entry")
        model.requestAnalysis(true)
        advanceUntilIdle()
        val freshModel = JournalViewModel(object : JournalApi {
            override suspend fun analyze(request: AnalyzeRequest) = result
        })
        assertTrue(freshModel.uiState.value.history.isEmpty())
        assertTrue(freshModel.uiState.value.input.isEmpty())
    }
}
