package com.example.androidinterview.payout.data.remote

import com.example.androidinterview.payout.data.remote.dto.CreatePayoutRequestDto
import com.example.androidinterview.payout.data.remote.dto.PayoutResponseDto

/** Remote source of payouts. */
interface PayoutRemoteDataSource {

    /**
     * `POST /api/payouts`: creates a payout.
     *
     * @param request Payout to create.
     * @throws java.io.IOException on network failure
     * @throws com.example.androidinterview.core.network.HttpException on a non-2xx response
     * @throws kotlinx.serialization.SerializationException on an unexpected payload
     */
    suspend fun createPayout(request: CreatePayoutRequestDto): PayoutResponseDto
}
