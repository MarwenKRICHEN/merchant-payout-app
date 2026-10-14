package com.example.androidinterview.merchant.domain

import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.model.ActivityPage
import com.example.androidinterview.merchant.domain.model.Merchant

interface MerchantRepository {

    /** Balances and the most recent activity of the merchant. */
    suspend fun getMerchant(): DataResult<Merchant>

    /**
     * One page of the merchant's activity.
     *
     * @param cursor [ActivityPage.nextCursor] of the previous page; null for the first page.
     * @param limit Page size; null to use the backend default.
     */
    suspend fun getActivityPage(
        cursor: String? = null,
        limit: Int? = null,
    ): DataResult<ActivityPage>
}
