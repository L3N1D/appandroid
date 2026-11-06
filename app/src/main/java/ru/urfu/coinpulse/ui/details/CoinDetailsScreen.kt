package ru.urfu.coinpulse.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.coinpulse.data.ChartRange
import ru.urfu.coinpulse.data.CoinDetails
import ru.urfu.coinpulse.ui.components.ChangeBadge
import ru.urfu.coinpulse.ui.components.CoinLogo
import ru.urfu.coinpulse.ui.components.ErrorState
import ru.urfu.coinpulse.ui.components.PriceChart
import ru.urfu.coinpulse.ui.components.SkeletonList
import ru.urfu.coinpulse.ui.components.changeColor
import ru.urfu.coinpulse.ui.format.asCompactCurrency
import ru.urfu.coinpulse.ui.format.asCompactNumber
import ru.urfu.coinpulse.ui.format.asPercent
import ru.urfu.coinpulse.ui.format.asPrice
import ru.urfu.coinpulse.ui.theme.Divider
import ru.urfu.coinpulse.ui.theme.Primary
import ru.urfu.coinpulse.ui.theme.PrimaryMuted
import ru.urfu.coinpulse.ui.theme.Surface
import ru.urfu.coinpulse.ui.theme.SurfaceElevated
import ru.urfu.coinpulse.ui.theme.SurfaceHighest
import ru.urfu.coinpulse.ui.theme.TextPrimary
import ru.urfu.coinpulse.ui.theme.TextSecondary
import ru.urfu.coinpulse.ui.theme.TextTertiary
import ru.urfu.coinpulse.ui.theme.Warning

@Composable
fun CoinDetailsRoute(
    onBack: () -> Unit,
    viewModel: CoinDetailsViewModel = viewModel(factory = CoinDetailsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoinDetailsScreen(
        state = state,
        onBack = onBack,
        onFavoriteClick = viewModel::onFavoriteClick,
        onRangeSelected = viewModel::onRangeSelected,
        onRetry = viewModel::loadDetails,
    )
}

@Composable
fun CoinDetailsScreen(
    state: CoinDetailsUiState,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRangeSelected: (ChartRange) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailsTopBar(
            title = state.details?.coin?.name ?: "Coin details",
            subtitle = state.details?.let { "${it.coin.symbol} · Rank #${it.coin.rank}" },
            isFavorite = state.isFavorite,
            onBack = onBack,
            onFavoriteClick = onFavoriteClick,
        )

        when {
            state.isLoading -> SkeletonList(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                count = 4,
            )

            state.error != null -> ErrorState(
                message = state.error ?: "Could not load the coin",
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )

            state.details != null -> DetailsContent(
                details = state.details,
                chartRange = state.chartRange,
                chartPoints = state.chartPoints,
                onRangeSelected = onRangeSelected,
            )
        }
    }
}

@Composable
private fun DetailsTopBar(
    title: String,
    subtitle: String?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
            }
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from watchlist" else "Add to watchlist",
                tint = if (isFavorite) Warning else TextSecondary,
            )
        }
    }
}

@Composable
private fun DetailsContent(
    details: CoinDetails,
    chartRange: ChartRange,
    chartPoints: List<Double>,
    onRangeSelected: (ChartRange) -> Unit,
) {
    val coin = details.coin
    val rangeChange = when (chartRange) {
        ChartRange.DAY -> coin.change24h
        ChartRange.WEEK -> coin.change7d
        ChartRange.MONTH -> details.change30d
        ChartRange.YEAR -> details.change1y
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CoinLogo(coin = coin, size = 46.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = coin.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = coin.symbol,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextTertiary,
                                )
                            }
                            Text(
                                text = "${coin.marketCap.asCompactCurrency()} market cap",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PrimaryMuted)
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "#${coin.rank}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = coin.price.asPrice(),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ChangeBadge(percent = rangeChange)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "past ${chartRange.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "H ${coin.high24h.asPrice()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    PriceChart(
                        points = chartPoints,
                        positive = rangeChange >= 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(178.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = chartPoints.minOrNull()?.asPrice().orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = chartPoints.maxOrNull()?.asPrice().orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    RangeSelector(
                        selected = chartRange,
                        onSelect = onRangeSelected,
                    )
                }
            }
        }

        item(key = "stats") {
            MarketStatsCard(
                details = details,
                rangeChange = rangeChange,
            )
        }

        item(key = "about") {
            AboutCoinCard(details = details)
        }

        item(key = "links") {
            LinksCard(details = details)
        }

        item(key = "categories") {
            CategoriesCard(categories = details.categories)
        }

        item(key = "meta") {
            MetaCard(details = details)
        }

        item(key = "footer") {
            Text(
                text = "Mock data shaped after the public CoinGecko API. Live network calls arrive in the next practice.",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun RangeSelector(
    selected: ChartRange,
    onSelect: (ChartRange) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceHighest)
            .padding(3.dp),
    ) {
        ChartRange.entries.forEach { range ->
            val active = range == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) PrimaryMuted else Color.Transparent)
                    .clickable { onSelect(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = range.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) Primary else TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun MarketStatsCard(
    details: CoinDetails,
    rangeChange: Double,
) {
    val coin = details.coin
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Market stats",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = rangeChange.asPercent(),
                    style = MaterialTheme.typography.labelLarge,
                    color = changeColor(rangeChange),
                )
            }
            Spacer(Modifier.height(14.dp))

            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                val marketCap = createRef()
                val fdv = createRef()
                val volume = createRef()
                val circulating = createRef()
                val ath = createRef()
                val atl = createRef()
                val middle = createGuidelineFromStart(0.5f)

                val gap = 10.dp
                val rowGap = 12.dp
                val cellHeight = 66.dp

                StatCell(
                    label = "Market cap",
                    value = coin.marketCap.asCompactCurrency(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .constrainAs(marketCap) {
                            width = Dimension.fillToConstraints
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(middle, margin = gap / 2)
                        },
                )
                StatCell(
                    label = "Fully diluted",
                    value = details.fullyDilutedValuation?.asCompactCurrency() ?: "—",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .constrainAs(fdv) {
                            width = Dimension.fillToConstraints
                            top.linkTo(parent.top)
                            start.linkTo(middle, margin = gap / 2)
                            end.linkTo(parent.end)
                        },
                )
                StatCell(
                    label = "24h volume",
                    value = coin.volume24h.asCompactCurrency(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .constrainAs(volume) {
                            width = Dimension.fillToConstraints
                            top.linkTo(marketCap.bottom, margin = rowGap)
                            start.linkTo(parent.start)
                            end.linkTo(middle, margin = gap / 2)
                        },
                )
                StatCell(
                    label = "Circulating",
                    value = "${coin.circulatingSupply.asCompactNumber()} ${coin.symbol}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .constrainAs(circulating) {
                            width = Dimension.fillToConstraints
                            top.linkTo(fdv.bottom, margin = rowGap)
                            start.linkTo(middle, margin = gap / 2)
                            end.linkTo(parent.end)
                        },
                )
                StatCell(
                    label = "All-time high",
                    value = coin.ath.asPrice(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .constrainAs(ath) {
                            width = Dimension.fillToConstraints
                            top.linkTo(volume.bottom, margin = rowGap)
                            start.linkTo(parent.start)
                            end.linkTo(middle, margin = gap / 2)
                        },
                )
                StatCell(
                    label = "All-time low",
                    value = coin.atl.asPrice(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .constrainAs(atl) {
                            width = Dimension.fillToConstraints
                            top.linkTo(circulating.bottom, margin = rowGap)
                            start.linkTo(middle, margin = gap / 2)
                            end.linkTo(parent.end)
                        },
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            maxLines = 1,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AboutCoinCard(details: CoinDetails) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = "About ${details.coin.name}",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = details.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = if (expanded) "Show less" else "Read more",
                    color = Primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinksCard(details: CoinDetails) {
    val uriHandler = LocalUriHandler.current
    val links = buildList {
        add("Website" to details.homepage)
        details.githubUrl?.let { add("GitHub" to it) }
        details.subredditUrl?.let { add("Reddit" to it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = "Official links",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                links.forEach { (label, url) ->
                    OutlinedButton(
                        onClick = {
                            runCatching { uriHandler.openUri(url) }
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Divider),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesCard(categories: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { category ->
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PrimaryMuted)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaCard(details: CoinDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            MetaRow("Genesis date", details.genesisDate)
            MetaDivider()
            MetaRow("Consensus / standard", details.hashingAlgorithm ?: "Fiat-backed")
            MetaDivider()
            MetaRow("Total supply", details.totalSupply?.let { "${it.asCompactNumber()} ${details.coin.symbol}" } ?: "—")
            MetaDivider()
            MetaRow("30d change", details.change30d.asPercent())
            MetaDivider()
            MetaRow("1y change", details.change1y.asPercent())
            MetaDivider()
            MetaRow("Last updated", details.lastUpdated)
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )
    }
}

@Composable
private fun MetaDivider() {
    HorizontalDivider(color = Divider.copy(alpha = 0.55f))
}
