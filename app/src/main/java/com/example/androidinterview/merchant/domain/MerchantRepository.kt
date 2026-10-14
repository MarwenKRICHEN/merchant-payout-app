package com.example.androidinterview.merchant.domain

import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.model.Merchant

interface MerchantRepository {

    /** Balances and the most recent activity of the merchant. */
    suspend fun getMerchant(): DataResult<Merchant>
}
