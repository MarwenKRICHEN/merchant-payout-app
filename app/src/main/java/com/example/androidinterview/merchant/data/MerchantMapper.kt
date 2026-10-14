package com.example.androidinterview.merchant.data

import com.example.androidinterview.merchant.data.remote.dto.ActivityItemDto
import com.example.androidinterview.merchant.data.remote.dto.ActivityStatusDto
import com.example.androidinterview.merchant.data.remote.dto.ActivityTypeDto
import com.example.androidinterview.merchant.data.remote.dto.CurrencyDto
import com.example.androidinterview.merchant.data.remote.dto.MerchantDataResponseDto
import com.example.androidinterview.merchant.data.remote.dto.PaginatedActivityResponseDto
import com.example.androidinterview.merchant.domain.model.Activity
import com.example.androidinterview.merchant.domain.model.ActivityPage
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Merchant
import com.example.androidinterview.merchant.domain.model.Money
import java.time.Instant

/** Maps the API response to the domain model; the account currency applies to both balances. */
internal fun MerchantDataResponseDto.toDomain(): Merchant {
    val currency = currency.toDomain()
    return Merchant(
        availableBalance = Money(availableBalance.toLong(), currency),
        pendingBalance = Money(pendingBalance.toLong(), currency),
        recentActivity = activity.map { it.toDomain() },
    )
}

/** Maps an activity page; the cursor is dropped when the API reports no more pages. */
internal fun PaginatedActivityResponseDto.toDomain() = ActivityPage(
    items = items.map { it.toDomain() },
    nextCursor = nextCursor.takeIf { hasMore },
)

/** Maps an API activity item, parsing its ISO 8601 date. */
internal fun ActivityItemDto.toDomain() = Activity(
    id = id,
    type = type.toDomain(),
    amount = Money(amount.toLong(), currency.toDomain()),
    date = Instant.parse(date),
    description = description,
    status = status.toDomain(),
)

/** Maps the API currency to the domain currency. */
internal fun CurrencyDto.toDomain() = when (this) {
    CurrencyDto.GBP -> Currency.GBP
    CurrencyDto.EUR -> Currency.EUR
}

/** Maps the API activity type to the domain type. */
internal fun ActivityTypeDto.toDomain() = when (this) {
    ActivityTypeDto.PAYOUT -> ActivityType.PAYOUT
    ActivityTypeDto.DEPOSIT -> ActivityType.DEPOSIT
    ActivityTypeDto.REFUND -> ActivityType.REFUND
    ActivityTypeDto.FEE -> ActivityType.FEE
}

/** Maps the API activity status to the domain status. */
internal fun ActivityStatusDto.toDomain() = when (this) {
    ActivityStatusDto.COMPLETED -> ActivityStatus.COMPLETED
    ActivityStatusDto.PENDING -> ActivityStatus.PENDING
    ActivityStatusDto.PROCESSING -> ActivityStatus.PROCESSING
    ActivityStatusDto.FAILED -> ActivityStatus.FAILED
}
