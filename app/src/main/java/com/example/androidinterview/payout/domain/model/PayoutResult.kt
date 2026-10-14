package com.example.androidinterview.payout.domain.model

/** Outcome of asking for a payout. */
sealed interface PayoutResult {

    /**
     * The payout was accepted.
     *
     * @param payout The created payout.
     */
    data class Success(val payout: Payout) : PayoutResult

    /**
     * The payout was not created.
     *
     * @param error Why it was not created.
     */
    data class Failure(val error: PayoutError) : PayoutResult
}

/** Why a payout could not be created. */
sealed interface PayoutError {

    /** No connectivity or any other transport failure. */
    data object Network : PayoutError

    /** The merchant's balance does not cover the amount. */
    data object InsufficientFunds : PayoutError

    /** The payout service is temporarily unavailable. */
    data object ServiceUnavailable : PayoutError

    /** The user dismissed the authentication a large payout requires. */
    data object AuthenticationCancelled : PayoutError

    /** Authentication is temporarily blocked after too many failed attempts. */
    data object AuthenticationLockedOut : PayoutError

    /** The device has no biometric enrolled. */
    data object AuthenticationNotEnrolled : PayoutError

    /** The device cannot authenticate with biometrics. */
    data object AuthenticationUnavailable : PayoutError

    /** Authentication failed for another reason. */
    data object AuthenticationFailed : PayoutError

    /** Any other failure, including an unexpected payload. */
    data object Unknown : PayoutError
}
