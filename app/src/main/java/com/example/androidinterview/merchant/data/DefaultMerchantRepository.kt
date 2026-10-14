package com.example.androidinterview.merchant.data

import com.example.androidinterview.core.network.safeApiCall
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.data.remote.MerchantRemoteDataSource
import com.example.androidinterview.merchant.domain.MerchantRepository
import com.example.androidinterview.merchant.domain.model.ActivityPage
import com.example.androidinterview.merchant.domain.model.Merchant

/**
 * [MerchantRepository] backed by the remote API.
 *
 * @param remote Remote source of the merchant data.
 */
class DefaultMerchantRepository(
    private val remote: MerchantRemoteDataSource,
) : MerchantRepository {

    override suspend fun getMerchant(): DataResult<Merchant> =
        safeApiCall { remote.getMerchant().toDomain() }

    override suspend fun getActivityPage(cursor: String?, limit: Int?): DataResult<ActivityPage> =
        safeApiCall { remote.getActivity(cursor, limit).toDomain() }
}
