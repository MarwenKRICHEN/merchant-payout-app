package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.merchant.data.remote.dto.ActivityItemDto
import com.example.androidinterview.merchant.data.remote.dto.ActivityStatusDto
import com.example.androidinterview.merchant.data.remote.dto.ActivityTypeDto
import com.example.androidinterview.merchant.data.remote.dto.CurrencyDto
import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto

/** In-memory [MerchantRemoteDataSource]: returns [response], or throws [error] when set. */
class FakeMerchantRemoteDataSource(
    var response: MerchantDataResponseDto = sampleResponse(),
    var error: Throwable? = null,
) : MerchantRemoteDataSource {

    var callCount = 0
        private set

    override suspend fun getMerchant(): MerchantDataResponseDto {
        callCount++
        error?.let { throw it }
        return response
    }

    companion object {
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
