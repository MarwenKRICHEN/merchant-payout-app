package com.example.androidinterview.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.androidinterview.merchant.ui.activity.ActivityRoute
import com.example.androidinterview.merchant.ui.home.HomeRoute
import com.example.androidinterview.payout.ui.PayoutRoute

/**
 * The app's navigation graph with its bottom navigation bar.
 *
 * Every destination owns its ViewModel through its back stack entry, so a ViewModel is created when
 * the destination is opened and cleared when it is popped. Each tab keeps its own state while the
 * user switches between tabs.
 *
 * @param modifier Modifier applied to the scaffold.
 * @param navController Controller driving the graph.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { AppNavigationBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable<HomeDestination> {
                HomeRoute(onShowActivity = { navController.navigate(ActivityDestination) })
            }
            composable<PayoutDestination> {
                PayoutRoute()
            }
            dialog<ActivityDestination>(
                dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                ActivityRoute(onDismiss = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Bottom bar switching between the top-level destinations, saving and restoring each tab's state.
 *
 * @param navController Controller used to navigate and to know the selected tab.
 */
@Composable
private fun AppNavigationBar(navController: NavHostController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(painterResource(destination.iconRes), contentDescription = null) },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}
