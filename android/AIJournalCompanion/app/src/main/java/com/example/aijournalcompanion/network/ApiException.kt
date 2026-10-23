package com.example.aijournalcompanion.network

/** Explicit failure types allow presentation code to choose appropriate UI feedback. */
sealed class ApiException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {
    class Connection(cause: Throwable) : ApiException("Cannot reach the journal backend.", cause)
    class Timeout(cause: Throwable) : ApiException("The analysis request timed out.", cause)
    class PermissionRequired :
        ApiException("Allow local network access before connecting to the journal backend.")
    class InvalidResponse(cause: Throwable? = null) :
        ApiException("The backend returned an invalid analysis response.", cause)

    /** Keep the HTTP status without exposing raw server errors or journal text in the UI. */
    class Http(val statusCode: Int) : ApiException(
        when (statusCode) {
            422 -> "The backend rejected the journal entry."
            503 -> "The local analysis service is unavailable."
            502 -> "The analysis service returned an invalid result."
            else -> "The backend could not complete the request (HTTP $statusCode)."
        },
    )
}
