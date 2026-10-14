package com.example.androidinterview.payout.data

import com.example.androidinterview.core.network.safeApiCall
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.payout.data.remote.PayoutRemoteDataSource
import com.example.androidinterview.payout.domain.PayoutRepository
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult

/**
 * [PayoutRepository] backed by the remote API.
 *
 * @param remote Remote source of payouts.
 */
class DefaultPayoutRepository(
    private val remote: PayoutRemoteDataSource,
) : PayoutRepository {

    override suspend fun createPayout(request: PayoutRequest): PayoutResult =
        when (val result = safeApiCall { remote.createPayout(request.toDto()).toDomain() }) {
            is DataResult.Success -> PayoutResult.Success(result.data)
            is DataResult.Failure -> PayoutResult.Failure(result.error.toPayoutError())
        }
}
