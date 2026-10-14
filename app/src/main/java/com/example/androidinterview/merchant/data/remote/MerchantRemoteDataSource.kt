package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto

interface MerchantRemoteDataSource {

    /**
     * `GET /api/merchant`: balances and the 3 most recent activity items.
     *
     * @throws java.io.IOException on network failure
     * @throws com.example.androidinterview.core.network.HttpException on a non-2xx response
     * @throws kotlinx.serialization.SerializationException on an unexpected payload
     */
    suspend fun getMerchant(): MerchantDataResponseDto
}
