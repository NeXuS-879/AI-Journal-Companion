package com.example.aijournalcompanion.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aijournalcompanion.network.ApiException
import com.example.aijournalcompanion.network.JournalApi
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.model.JournalEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Coordinates validation and one request at a time; HTTP and UI remain separate. */
class JournalViewModel(private val api: JournalApi) : ViewModel() {
    private val mutableState = MutableStateFlow(JournalUiState())
    val uiState = mutableState.asStateFlow()

    fun onInputChanged(text: String) {
        if (mutableState.value.isBusy) return
        val length = text.trim().let { it.codePointCount(0, it.length) }
        mutableState.update {
            it.copy(
                input = text,
                inputError = if (length > AnalyzeRequest.MAX_TEXT_LENGTH) {
                    JournalInputError.TOO_LONG
                } else null,
                result = null,
                error = null,
            )
        }
    }

    /** Returns true only when the UI should launch the Android permission prompt. */
    fun requestAnalysis(hasLocalNetworkPermission: Boolean): Boolean {
        val current = mutableState.value
        if (current.isBusy) return false
        val inputError = when {
            current.input.isBlank() -> JournalInputError.EMPTY
            current.characterCount > AnalyzeRequest.MAX_TEXT_LENGTH -> JournalInputError.TOO_LONG
            else -> null
        }
        mutableState.update { it.copy(inputError = inputError, error = null, result = null) }
        if (inputError != null) return false
        if (!hasLocalNetworkPermission) {
            mutableState.update { it.copy(awaitingPermission = true) }
            return true
        }
        analyze()
        return false
    }

    fun onPermissionResult(granted: Boolean) {
        if (!mutableState.value.awaitingPermission) return
        mutableState.update { it.copy(awaitingPermission = false) }
        if (granted) analyze()
        else mutableState.update { it.copy(error = JournalAnalysisError.PERMISSION) }
    }

    private fun analyze() {
        val request = AnalyzeRequest(mutableState.value.input.trim())
        // Set synchronously so repeated taps cannot enqueue duplicate calls.
        mutableState.update { it.copy(isLoading = true, result = null, error = null) }
        viewModelScope.launch {
            try {
                val response = api.analyze(request)
                // Create identity once, outside the retryable StateFlow update lambda.
                val entry = JournalEntry(
                    text = request.text,
                    emotion = response.emotion,
                    advice = response.advice,
                )
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        result = response,
                        history = listOf(entry) + it.history,
                    )
                }
            } catch (cancelled: CancellationException) {
                mutableState.update { it.copy(isLoading = false) }
                throw cancelled
            } catch (exception: Exception) {
                mutableState.update { it.copy(isLoading = false, error = exception.toUiError()) }
            }
        }
    }

    private fun Exception.toUiError(): JournalAnalysisError = when (this) {
        is ApiException.Connection -> JournalAnalysisError.CONNECTION
        is ApiException.Timeout -> JournalAnalysisError.TIMEOUT
        is ApiException.PermissionRequired -> JournalAnalysisError.PERMISSION
        is ApiException.InvalidResponse -> JournalAnalysisError.INVALID_RESPONSE
        is ApiException.Http -> when (statusCode) {
            422 -> JournalAnalysisError.INVALID_ENTRY
            503 -> JournalAnalysisError.SERVICE_UNAVAILABLE
            502 -> JournalAnalysisError.INVALID_RESPONSE
            else -> JournalAnalysisError.SERVER
        }
        else -> JournalAnalysisError.UNEXPECTED
    }

    class Factory(private val api: JournalApi) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(JournalViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(api) as T
        }
    }
}
