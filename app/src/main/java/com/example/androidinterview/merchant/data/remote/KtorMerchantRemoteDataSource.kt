package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.core.network.getJson
import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto
import com.example.androidinterview.merchant.data.remote.dto.PaginatedActivityResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

private const val MERCHANT_PATH = "api/merchant"
private const val ACTIVITY_PATH = "api/merchant/activity"
private const val PARAM_CURSOR = "cursor"
private const val PARAM_LIMIT = "limit"

/**
 * [MerchantRemoteDataSource] that calls the API with a Ktor [HttpClient].
 *
 * @param client Client configured with the API base URL.
 */
class KtorMerchantRemoteDataSource(
    private val client: HttpClient,
) : MerchantRemoteDataSource {

    override suspend fun getMerchant(): MerchantDataResponseDto = client.getJson(MERCHANT_PATH)

    override suspend fun getActivity(cursor: String?, limit: Int?): PaginatedActivityResponseDto =
        client.getJson(ACTIVITY_PATH) {
            parameter(PARAM_CURSOR, cursor)
            parameter(PARAM_LIMIT, limit)
        }
}
