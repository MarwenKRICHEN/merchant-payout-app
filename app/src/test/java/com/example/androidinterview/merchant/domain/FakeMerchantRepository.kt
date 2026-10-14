package com.example.androidinterview.merchant.domain

import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.model.Merchant

/** Returns [result] on every call and counts the calls. */
class FakeMerchantRepository(
    var result: DataResult<Merchant>,
) : MerchantRepository {

    var callCount = 0
        private set

    override suspend fun getMerchant(): DataResult<Merchant> {
        callCount++
        return result
    }
}
