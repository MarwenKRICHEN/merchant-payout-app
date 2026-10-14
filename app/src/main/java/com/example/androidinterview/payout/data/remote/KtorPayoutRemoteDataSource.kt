package com.example.androidinterview.payout.data.remote

import com.example.androidinterview.core.network.postJson
import com.example.androidinterview.payout.data.remote.dto.CreatePayoutRequestDto
import com.example.androidinterview.payout.data.remote.dto.PayoutResponseDto
import io.ktor.client.HttpClient

private const val PAYOUTS_PATH = "api/payouts"

/**
 * [PayoutRemoteDataSource] that calls the API with a Ktor [HttpClient].
 *
 * @param client Client configured with the API base URL.
 */
class KtorPayoutRemoteDataSource(
    private val client: HttpClient,
) : PayoutRemoteDataSource {

    override suspend fun createPayout(request: CreatePayoutRequestDto): PayoutResponseDto =
        client.postJson(PAYOUTS_PATH, request)
}
