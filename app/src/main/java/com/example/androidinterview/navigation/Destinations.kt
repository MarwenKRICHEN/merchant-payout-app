package com.example.androidinterview.navigation

import kotlinx.serialization.Serializable

/** Merchant overview: balances and the most recent activity. */
@Serializable
data object HomeDestination

/** Full list of the merchant's activity, shown on top of [HomeDestination]. */
@Serializable
data object ActivityDestination
