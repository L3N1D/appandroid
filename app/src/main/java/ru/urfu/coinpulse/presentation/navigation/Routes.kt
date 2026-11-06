package ru.urfu.coinpulse.presentation.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val MARKET = "market"
    const val WATCHLIST = "watchlist"
    const val ABOUT = "about"

    const val COIN_ID_ARG = "coinId"
    const val COIN_DETAILS = "coin/{$COIN_ID_ARG}"

    fun coinDetails(coinId: String): String = "coin/${Uri.encode(coinId)}"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    MARKET(Routes.MARKET, "Markets", Icons.AutoMirrored.Filled.List),
    WATCHLIST(Routes.WATCHLIST, "Watchlist", Icons.Filled.Star),
    ABOUT(Routes.ABOUT, "About", Icons.Filled.Info),
}

val topLevelRoutes: Set<String> = TopLevelDestination.entries.map { it.route }.toSet()
