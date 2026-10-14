package com.example.androidinterview.authentication

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Links code that needs an authentication (no UI, no Activity) to the UI that can show the prompt.
 *
 * [request] publishes a [PendingAuthentication] in [pending] and suspends until the UI completes it.
 * The request lives here, not in the Activity, so it survives a configuration change: the recreated
 * UI observes [pending] again and shows the prompt again. One request is served at a time.
 */
class AuthenticationBridge {

    private val mutex = Mutex()
    private val _pending = MutableStateFlow<PendingAuthentication?>(null)

    /** The request waiting for the UI, or null. */
    val pending: StateFlow<PendingAuthentication?> = _pending.asStateFlow()

    /** Publishes a request and suspends until the UI completes it; cancelling clears the request. */
    suspend fun request(): AuthenticationResult = mutex.withLock {
        val result = CompletableDeferred<AuthenticationResult>()
        _pending.value = PendingAuthentication(result)
        try {
            result.await()
        } finally {
            _pending.value = null
        }
    }
}

/**
 * A request the UI has to answer.
 *
 * Compared by identity so a UI effect keyed on it restarts for each new request.
 *
 * @param result Completed with the outcome.
 */
class PendingAuthentication internal constructor(
    private val result: CompletableDeferred<AuthenticationResult>,
) {

    /**
     * Whether the UI already shows the prompt for this request, so that a recreated UI does not show
     * it a second time.
     */
    var isPromptShown: Boolean = false

    /**
     * Answers the request.
     *
     * @param outcome What happened.
     */
    fun complete(outcome: AuthenticationResult) {
        result.complete(outcome)
    }
}
