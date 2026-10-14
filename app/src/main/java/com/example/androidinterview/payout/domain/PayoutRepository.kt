package com.example.androidinterview.payout.domain

import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult

/** Source of payouts. */
interface PayoutRepository {

    /**
     * Asks the API to create a payout.
     *
     * @param request What to send and where.
     */
    suspend fun createPayout(request: PayoutRequest): PayoutResult
}
