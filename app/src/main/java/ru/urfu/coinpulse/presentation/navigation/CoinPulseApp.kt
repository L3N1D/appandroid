package ru.urfu.coinpulse.presentation.navigation

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
import ru.urfu.coinpulse.presentation.about.AboutScreen
import ru.urfu.coinpulse.presentation.details.CoinDetailsRoute
import ru.urfu.coinpulse.presentation.filters.FilterRoute
import ru.urfu.coinpulse.presentation.market.MarketRoute
import ru.urfu.coinpulse.presentation.profile.EditProfileRoute
import ru.urfu.coinpulse.presentation.profile.ProfileRoute
import ru.urfu.coinpulse.presentation.theme.Background
import ru.urfu.coinpulse.presentation.theme.Primary
import ru.urfu.coinpulse.presentation.theme.PrimaryMuted
import ru.urfu.coinpulse.presentation.theme.Surface
import ru.urfu.coinpulse.presentation.theme.TextTertiary
import ru.urfu.coinpulse.presentation.watchlist.WatchlistRoute

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
                    onFiltersClick = {
                        navController.navigate(Routes.FILTERS)
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

            composable(Routes.PROFILE) {
                ProfileRoute(
                    onEdit = { navController.navigate(Routes.PROFILE_EDIT) },
                )
            }

            composable(Routes.PROFILE_EDIT) {
                EditProfileRoute(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(Routes.FILTERS) {
                FilterRoute(
                    onBack = { navController.popBackStack() },
                )
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
