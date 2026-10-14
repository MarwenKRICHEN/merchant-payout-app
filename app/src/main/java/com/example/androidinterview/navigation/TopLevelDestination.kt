package com.example.androidinterview.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.androidinterview.R
import kotlin.reflect.KClass

/**
 * A destination reachable from the bottom navigation bar.
 *
 * @param route Destination opened by the tab.
 * @param routeClass Type of [route], used to find out whether the tab is selected.
 * @param labelRes Tab label.
 * @param iconRes Tab icon.
 */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    Home(HomeDestination, HomeDestination::class, R.string.nav_home, R.drawable.ic_home),
    Payouts(PayoutDestination, PayoutDestination::class, R.string.nav_payouts, R.drawable.ic_payouts),
}
