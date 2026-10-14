package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.core.network.getJson
import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto
import io.ktor.client.HttpClient

private const val MERCHANT_PATH = "api/merchant"

/**
 * [MerchantRemoteDataSource] that calls the API with a Ktor [HttpClient].
 *
 * @param client Client configured with the API base URL.
 */
class KtorMerchantRemoteDataSource(
    private val client: HttpClient,
) : MerchantRemoteDataSource {

    override suspend fun getMerchant(): MerchantDataResponseDto = client.getJson(MERCHANT_PATH)
}
