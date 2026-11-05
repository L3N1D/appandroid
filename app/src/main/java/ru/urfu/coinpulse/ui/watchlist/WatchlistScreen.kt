package ru.urfu.coinpulse.ui.watchlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.coinpulse.ui.components.CoinRow
import ru.urfu.coinpulse.ui.components.EmptyState
import ru.urfu.coinpulse.ui.components.SkeletonList
import ru.urfu.coinpulse.ui.format.asCompactCurrency
import ru.urfu.coinpulse.ui.theme.Divider
import ru.urfu.coinpulse.ui.theme.Primary
import ru.urfu.coinpulse.ui.theme.Surface
import ru.urfu.coinpulse.ui.theme.TextPrimary
import ru.urfu.coinpulse.ui.theme.TextSecondary
import ru.urfu.coinpulse.ui.theme.TextTertiary

@Composable
fun WatchlistRoute(
    onCoinClick: (String) -> Unit,
    onExplore: () -> Unit,
    viewModel: WatchlistViewModel = viewModel(factory = WatchlistViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WatchlistScreen(
        state = state,
        onCoinClick = onCoinClick,
        onRemoveFavorite = viewModel::onRemoveFavorite,
        onExplore = onExplore,
    )
}

@Composable
fun WatchlistScreen(
    state: WatchlistUiState,
    onCoinClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onExplore: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 10.dp),
        ) {
            Text(
                text = "Watchlist",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Text(
                text = "Keep an eye on the coins that matter to you",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }

        when {
            state.isLoading -> SkeletonList(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                count = 3,
            )

            state.coins.isEmpty() -> EmptyState(
                title = "Your watchlist is empty",
                message = "Open any coin and tap the heart to pin it here. Your selection is shared across all screens.",
                icon = Icons.Filled.Favorite,
                actionLabel = "Explore markets",
                onAction = onExplore,
                modifier = Modifier.weight(1f),
            )

            else -> {
                WatchlistSummary(
                    count = state.coins.size,
                    totalMarketCap = state.totalMarketCap,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.padding(top = 12.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    items(
                        items = state.coins,
                        key = { coin -> coin.id },
                    ) { coin ->
                        CoinRow(
                            coin = coin,
                            onClick = { onCoinClick(coin.id) },
                            isFavorite = true,
                            onFavoriteClick = { onRemoveFavorite(coin.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistSummary(
    count: Int,
    totalMarketCap: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "COMBINED MARKET CAP",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
                Text(
                    text = totalMarketCap.asCompactCurrency(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TRACKING",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
                Text(
                    text = "$count ${if (count == 1) "asset" else "assets"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                )
            }
        }
    }
}
