package com.example.androidinterview.merchant.domain

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.model.ActivityPage
import com.example.androidinterview.merchant.domain.model.Merchant
import kotlinx.coroutines.CompletableDeferred

/**
 * Scripted [MerchantRepository].
 *
 * @param result Returned by every [getMerchant] call.
 * @param activityResults Returned by [getActivityPage], keyed by cursor (null for the first page).
 */
class FakeMerchantRepository(
    var result: DataResult<Merchant> = DataResult.Failure(DataError.Unknown),
    val activityResults: MutableMap<String?, DataResult<ActivityPage>> = mutableMapOf(),
) : MerchantRepository {

    var callCount = 0
        private set

    /** Cursors of every [getActivityPage] call, in order. */
    val activityRequests = mutableListOf<String?>()

    /** When set, [getActivityPage] suspends until it completes, to observe in-flight states. */
    var activityGate: CompletableDeferred<Unit>? = null

    override suspend fun getMerchant(): DataResult<Merchant> {
        callCount++
        return result
    }

    override suspend fun getActivityPage(cursor: String?, limit: Int?): DataResult<ActivityPage> {
        activityRequests += cursor
        activityGate?.await()
        return activityResults.getValue(cursor)
    }
}
