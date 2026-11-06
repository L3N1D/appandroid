package ru.urfu.coinpulse.presentation.market

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinSort
import ru.urfu.coinpulse.presentation.components.CoinRow
import ru.urfu.coinpulse.presentation.components.EmptyState
import ru.urfu.coinpulse.presentation.components.ErrorState
import ru.urfu.coinpulse.presentation.components.InlineLoader
import ru.urfu.coinpulse.presentation.components.SkeletonList
import ru.urfu.coinpulse.presentation.theme.Divider
import ru.urfu.coinpulse.presentation.theme.Negative
import ru.urfu.coinpulse.presentation.theme.Primary
import ru.urfu.coinpulse.presentation.theme.PrimaryMuted
import ru.urfu.coinpulse.presentation.theme.Surface
import ru.urfu.coinpulse.presentation.theme.TextPrimary
import ru.urfu.coinpulse.presentation.theme.TextSecondary
import ru.urfu.coinpulse.presentation.theme.TextTertiary

@Composable
fun MarketRoute(
    onCoinClick: (String) -> Unit,
    onFiltersClick: () -> Unit,
    viewModel: MarketViewModel = viewModel(factory = MarketViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MarketScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::loadMore,
        onFavoriteClick = viewModel::onFavoriteClick,
        onCoinClick = onCoinClick,
        onFiltersClick = onFiltersClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    state: MarketUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (CoinSort) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onFavoriteClick: (String) -> Unit,
    onCoinClick: (String) -> Unit,
    onFiltersClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader(
            total = state.total,
            isRefreshing = state.isRefreshing,
            showFilterBadge = state.showFilterBadge,
            onRefresh = onRefresh,
            onFiltersClick = onFiltersClick,
        )
        MarketSearchField(
            query = state.query,
            onQueryChange = onQueryChange,
        )
        SortChips(
            selected = state.sort,
            onSortChange = onSortChange,
        )
        ResultsCaption(
            query = state.query,
            loaded = state.coins.size,
            total = state.total,
            sort = state.sort,
        )

        state.error?.takeIf { state.coins.isNotEmpty() }?.let { message ->
            ErrorBanner(
                message = message,
                onRetry = onRetry,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> SkeletonList(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )

                state.error != null && state.coins.isEmpty() -> ErrorState(
                    message = state.error ?: "Could not load the market",
                    onRetry = onRetry,
                )

                state.coins.isEmpty() -> EmptyState(
                    title = if (state.filters.isActive) "No coins match filters" else "No coins found",
                    message = when {
                        state.filters.isActive ->
                            "Try lowering the market cap or volume threshold, or change the 24h performance filter."
                        state.query.isNotBlank() ->
                            "Nothing matches “${state.query}”. Try another name or ticker."
                        else -> "Pull to refresh the live CoinGecko feed."
                    },
                    icon = Icons.Filled.Search,
                    actionLabel = if (state.filters.isActive) "Edit filters" else "Clear search",
                    onAction = {
                        if (state.filters.isActive) onFiltersClick() else onQueryChange("")
                    },
                )

                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CoinList(
                        coins = state.coins,
                        favoriteIds = state.favoriteIds,
                        hasMore = state.hasMore,
                        total = state.total,
                        isLoadingMore = state.isLoadingMore,
                        onLoadMore = onLoadMore,
                        onFavoriteClick = onFavoriteClick,
                        onCoinClick = onCoinClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketHeader(
    total: Int,
    isRefreshing: Boolean,
    showFilterBadge: Boolean,
    onRefresh: () -> Unit,
    onFiltersClick: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "live-dot")
    val dotAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "live-dot-alpha",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Markets",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = dotAlpha)),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = if (total > 0) {
                    "$total ${if (total == 1) "asset" else "assets"} · CoinGecko live feed"
                } else {
                    "CoinGecko live feed"
                },
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }
        IconButton(onClick = onFiltersClick) {
            Box {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Filters",
                    tint = if (showFilterBadge) Primary else TextSecondary,
                )
                if (showFilterBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Negative),
                    )
                }
            }
        }
        IconButton(onClick = onRefresh) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    strokeWidth = 2.dp,
                    color = Primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh market",
                    tint = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun MarketSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        placeholder = {
            Text(
                text = "Search Bitcoin, ETH, layer 1…",
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TextSecondary,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Primary,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Primary.copy(alpha = 0.55f),
            unfocusedBorderColor = Divider,
        ),
    )
}

@Composable
private fun SortChips(
    selected: CoinSort,
    onSortChange: (CoinSort) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Sort",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.padding(end = 2.dp),
        )
        CoinSort.entries.forEach { sort ->
            FilterChip(
                selected = sort == selected,
                onClick = { onSortChange(sort) },
                label = {
                    Text(
                        text = sort.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Surface,
                    labelColor = TextSecondary,
                    selectedContainerColor = PrimaryMuted,
                    selectedLabelColor = Primary,
                ),
            )
        }
    }
}

@Composable
private fun ResultsCaption(
    query: String,
    loaded: Int,
    total: Int,
    sort: CoinSort,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = when {
                query.isNotBlank() -> "Results for “$query”"
                sort == CoinSort.MARKET_CAP -> "Top assets by market cap"
                else -> "Sorted by ${sort.label.lowercase()}"
            },
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (total > 0) {
            Text(
                text = "$loaded / $total",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }
    }
}

@Composable
private fun CoinList(
    coins: List<Coin>,
    favoriteIds: Set<String>,
    hasMore: Boolean,
    total: Int,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onFavoriteClick: (String) -> Unit,
    onCoinClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            listState.firstVisibleItemIndex > 0 &&
                info.totalItemsCount > 0 &&
                lastVisibleIndex >= info.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore, hasMore, coins.size) {
        if (shouldLoadMore && hasMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        items(
            items = coins,
            key = { coin -> coin.id },
        ) { coin ->
            CoinRow(
                coin = coin,
                onClick = { onCoinClick(coin.id) },
                isFavorite = coin.id in favoriteIds,
                onFavoriteClick = { onFavoriteClick(coin.id) },
            )
        }

        if (isLoadingMore) {
            item(key = "loading-more") {
                InlineLoader()
            }
        } else if (!hasMore && total > 10 && coins.isNotEmpty()) {
            item(key = "end-of-list") {
                Text(
                    text = "You have reached the end of the live feed",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Negative.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, Negative.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Negative,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Couldn't refresh",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            TextButton(onClick = onRetry) {
                Text(
                    text = "Retry",
                    color = Negative,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
