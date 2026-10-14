package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto
import com.example.androidinterview.merchant.data.remote.dto.PaginatedActivityResponseDto

interface MerchantRemoteDataSource {

    /**
     * `GET /api/merchant`: balances and the 3 most recent activity items.
     *
     * @throws java.io.IOException on network failure
     * @throws com.example.androidinterview.core.network.HttpException on a non-2xx response
     * @throws kotlinx.serialization.SerializationException on an unexpected payload
     */
    suspend fun getMerchant(): MerchantDataResponseDto

    /**
     * `GET /api/merchant/activity`: one page of activity, using cursor-based pagination.
     *
     * @param cursor `next_cursor` of the previous page; null for the first page (not sent).
     * @param limit Page size; null to let the backend use its default (not sent).
     * @throws java.io.IOException on network failure
     * @throws com.example.androidinterview.core.network.HttpException on a non-2xx response
     * @throws kotlinx.serialization.SerializationException on an unexpected payload
     */
    suspend fun getActivity(
        cursor: String? = null,
        limit: Int? = null,
    ): PaginatedActivityResponseDto
}
