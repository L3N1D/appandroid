package ru.urfu.coinpulse.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.urfu.coinpulse.ui.about.AboutScreen
import ru.urfu.coinpulse.ui.details.CoinDetailsRoute
import ru.urfu.coinpulse.ui.market.MarketRoute
import ru.urfu.coinpulse.ui.theme.Background
import ru.urfu.coinpulse.ui.theme.Primary
import ru.urfu.coinpulse.ui.theme.PrimaryMuted
import ru.urfu.coinpulse.ui.theme.Surface
import ru.urfu.coinpulse.ui.theme.TextTertiary
import ru.urfu.coinpulse.ui.watchlist.WatchlistRoute

@Composable
fun CoinPulseApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == null || currentRoute in topLevelRoutes

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                CoinPulseBottomBar(
                    currentRoute = currentRoute,
                    onDestinationClick = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MARKET,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.MARKET) {
                MarketRoute(
                    onCoinClick = { coinId ->
                        navController.navigate(Routes.coinDetails(coinId))
                    },
                )
            }

            composable(Routes.WATCHLIST) {
                WatchlistRoute(
                    onCoinClick = { coinId ->
                        navController.navigate(Routes.coinDetails(coinId))
                    },
                    onExplore = {
                        navController.navigate(Routes.MARKET) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen()
            }

            composable(
                route = Routes.COIN_DETAILS,
                arguments = listOf(
                    navArgument(Routes.COIN_ID_ARG) { type = NavType.StringType },
                ),
            ) {
                CoinDetailsRoute(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun CoinPulseBottomBar(
    currentRoute: String?,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onDestinationClick(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryMuted,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                ),
            )
        }
    }
}
