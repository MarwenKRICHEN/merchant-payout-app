package com.example.androidinterview.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.androidinterview.merchant.ui.activity.ActivityRoute
import com.example.androidinterview.merchant.ui.home.HomeRoute

/**
 * The app's navigation graph.
 *
 * Every destination owns its ViewModel through its back stack entry, so a ViewModel is created when
 * the destination is opened and cleared when it is popped.
 *
 * @param modifier Modifier applied to the host.
 * @param navController Controller driving the graph.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
    ) {
        composable<HomeDestination> {
            HomeRoute(onShowActivity = { navController.navigate(ActivityDestination) })
        }
        dialog<ActivityDestination>(
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            ActivityRoute(onDismiss = { navController.popBackStack() })
        }
    }
}
