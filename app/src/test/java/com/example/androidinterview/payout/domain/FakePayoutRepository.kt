package com.example.androidinterview.payout.domain

import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult
import kotlinx.coroutines.CompletableDeferred

/**
 * Scripted [PayoutRepository].
 *
 * @param result Returned by every [createPayout] call.
 */
class FakePayoutRepository(
    var result: PayoutResult = PayoutResult.Failure(PayoutError.Unknown),
) : PayoutRepository {

    /** Every request received, in order. */
    val requests = mutableListOf<PayoutRequest>()

    /** When set, [createPayout] suspends until it completes, to observe the in-flight state. */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun createPayout(request: PayoutRequest): PayoutResult {
        requests += request
        gate?.await()
        return result
    }
}
