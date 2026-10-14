package com.example.androidinterview.merchant.domain.model

import com.example.androidinterview.money.Money

/**
 * The merchant's financial overview.
 *
 * @param availableBalance Balance available for payouts.
 * @param pendingBalance Balance not yet settled.
 * @param recentActivity The most recent transactions.
 */
data class Merchant(
    val availableBalance: Money,
    val pendingBalance: Money,
    val recentActivity: List<Activity>,
)
