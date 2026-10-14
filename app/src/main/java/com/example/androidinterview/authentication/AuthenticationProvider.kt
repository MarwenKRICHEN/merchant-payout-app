package com.example.androidinterview.authentication

/** Confirms that the person holding the device is allowed to perform a sensitive action. */
interface AuthenticationProvider {

    /** Asks the user to authenticate and suspends until the outcome is known. */
    suspend fun authenticate(): AuthenticationResult
}

/** Outcome of an authentication attempt. */
sealed interface AuthenticationResult {
    /** The user authenticated. */
    data object Succeeded : AuthenticationResult

    /** The user dismissed the prompt. */
    data object Cancelled : AuthenticationResult

    /** Too many failed attempts; authentication is temporarily blocked. */
    data object LockedOut : AuthenticationResult

    /** The device has no biometric enrolled. */
    data object NotEnrolled : AuthenticationResult

    /** The device cannot authenticate with biometrics (no hardware, or temporarily unusable). */
    data object Unavailable : AuthenticationResult

    /** Authentication failed for another reason. */
    data object Failed : AuthenticationResult
}
