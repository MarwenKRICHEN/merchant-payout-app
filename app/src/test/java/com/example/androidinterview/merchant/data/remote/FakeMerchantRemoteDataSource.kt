package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.merchant.data.remote.dto.ActivityItemDto
import com.example.androidinterview.merchant.data.remote.dto.ActivityStatusDto
import com.example.androidinterview.merchant.data.remote.dto.ActivityTypeDto
import com.example.androidinterview.merchant.data.remote.dto.CurrencyDto
import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto
import com.example.androidinterview.merchant.data.remote.dto.PaginatedActivityResponseDto

/**
 * In-memory [MerchantRemoteDataSource]: returns [response] / [activityResponse], or throws [error] when set.
 *
 * @param response Returned by [getMerchant].
 * @param activityResponse Returned by [getActivity].
 * @param error Thrown by every call when set.
 */
class FakeMerchantRemoteDataSource(
    var response: MerchantDataResponseDto = sampleResponse(),
    var activityResponse: PaginatedActivityResponseDto = sampleActivityResponse(),
    var error: Throwable? = null,
) : MerchantRemoteDataSource {

    var callCount = 0
        private set

    /** Arguments of every [getActivity] call, in order. */
    val activityRequests = mutableListOf<Pair<String?, Int?>>()

    override suspend fun getMerchant(): MerchantDataResponseDto {
        callCount++
        error?.let { throw it }
        return response
    }

    override suspend fun getActivity(cursor: String?, limit: Int?): PaginatedActivityResponseDto {
        activityRequests += cursor to limit
        error?.let { throw it }
        return activityResponse
    }

    companion object {
        /** A page made of the sample activity, with more pages after it. */
        fun sampleActivityResponse() = PaginatedActivityResponseDto(
            items = sampleResponse().activity,
            nextCursor = "act_003",
            hasMore = true,
        )

        /** Same values as `app/android/fixtures/merchant.json`. */
        fun sampleResponse() = MerchantDataResponseDto(
            availableBalance = 500000,
            pendingBalance = 25000,
            currency = CurrencyDto.GBP,
            activity = listOf(
                ActivityItemDto(
                    id = "act_001",
                    type = ActivityTypeDto.DEPOSIT,
                    amount = 150000,
                    currency = CurrencyDto.GBP,
                    date = "2026-05-18T10:23:00.000Z",
                    description = "Payment from Customer ABC",
                    status = ActivityStatusDto.COMPLETED,
                ),
                ActivityItemDto(
                    id = "act_002",
                    type = ActivityTypeDto.PAYOUT,
                    amount = -50000,
                    currency = CurrencyDto.GBP,
                    date = "2026-05-17T14:30:00.000Z",
                    description = "Payout to Bank Account ****1234",
                    status = ActivityStatusDto.COMPLETED,
                ),
                ActivityItemDto(
                    id = "act_003",
                    type = ActivityTypeDto.DEPOSIT,
                    amount = 230000,
                    currency = CurrencyDto.GBP,
                    date = "2026-05-16T09:15:00.000Z",
                    description = "Payment from Customer XYZ",
                    status = ActivityStatusDto.COMPLETED,
                ),
            ),
        )
    }
}
