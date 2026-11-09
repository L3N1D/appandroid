package ru.urfu.coinpulse.presentation.filters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.coinpulse.domain.model.PerformanceFilter
import ru.urfu.coinpulse.presentation.theme.Divider
import ru.urfu.coinpulse.presentation.theme.Negative
import ru.urfu.coinpulse.presentation.theme.Primary
import ru.urfu.coinpulse.presentation.theme.PrimaryMuted
import ru.urfu.coinpulse.presentation.theme.Surface
import ru.urfu.coinpulse.presentation.theme.SurfaceElevated
import ru.urfu.coinpulse.presentation.theme.SurfaceHighest
import ru.urfu.coinpulse.presentation.theme.TextPrimary
import ru.urfu.coinpulse.presentation.theme.TextSecondary
import ru.urfu.coinpulse.presentation.theme.TextTertiary

@Composable
fun FilterRoute(
    onBack: () -> Unit,
    viewModel: FilterViewModel = viewModel(factory = FilterViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event == FilterEvent.APPLIED) onBack()
        }
    }

    FilterScreen(
        state = state,
        onBack = onBack,
        onMinMarketCapChange = viewModel::setMinMarketCap,
        onPerformanceChange = viewModel::setPerformance,
        onMinVolumeChange = viewModel::setMinVolume24h,
        onResetDraft = viewModel::resetDraft,
        onApply = viewModel::applyFilters,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    state: FilterUiState,
    onBack: () -> Unit,
    onMinMarketCapChange: (Long) -> Unit,
    onPerformanceChange: (PerformanceFilter) -> Unit,
    onMinVolumeChange: (Long) -> Unit,
    onResetDraft: () -> Unit,
    onApply: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    if (state.draftIsActive) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Negative),
                        )
                    }
                }
                Text(
                    text = "Saved on this device with DataStore",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BadgeInfoCard(
                badgeVisible = state.badgeVisible,
                draftIsActive = state.draftIsActive,
                hasChanges = state.hasChanges,
                saveError = state.saveError,
            )

            FilterSection(
                title = "Minimum market cap",
                description = "Hide smaller assets from the Markets list.",
            ) {
                FilterOptionRow(
                    options = MARKET_CAP_OPTIONS,
                    selected = state.draft.minMarketCap,
                    onSelected = onMinMarketCapChange,
                )
            }

            FilterSection(
                title = "24h performance",
                description = "Show all coins, only gainers or only losers.",
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PerformanceFilter.entries.forEach { performance ->
                        FilterChip(
                            selected = state.draft.performance == performance,
                            onClick = { onPerformanceChange(performance) },
                            label = { Text(performance.label) },
                            colors = filterChipColors(),
                        )
                    }
                }
            }

            FilterSection(
                title = "Minimum 24h volume",
                description = "Filter out thinly traded coins.",
            ) {
                FilterOptionRow(
                    options = VOLUME_OPTIONS,
                    selected = state.draft.minVolume24h,
                    onSelected = onMinVolumeChange,
                )
            }

            Text(
                text = "Filters are stored in DataStore and applied to the cached CoinGecko snapshot. The badge is a shared in-memory cache between Markets and Filters.",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onResetDraft,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Divider),
            ) {
                Text("Reset", color = TextSecondary)
            }
            Button(
                onClick = onApply,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Apply filters", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BadgeInfoCard(
    badgeVisible: Boolean,
    draftIsActive: Boolean,
    hasChanges: Boolean,
    saveError: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (badgeVisible) PrimaryMuted else SurfaceHighest),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (badgeVisible) Negative else TextTertiary),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (badgeVisible) "Badge is visible on Markets" else "No badge on Markets",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = when {
                        hasChanges -> "Unsaved changes — apply to update the badge."
                        draftIsActive -> "Filters are active and persist between launches."
                        else -> "Filters are in the default state."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                if (saveError != null) {
                    Text(
                        text = saveError,
                        style = MaterialTheme.typography.labelMedium,
                        color = Negative,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary,
        )
        Spacer(Modifier.height(2.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterOptionRow(
    options: List<Pair<Long, String>>,
    selected: Long,
    onSelected: (Long) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(label) },
                colors = filterChipColors(),
            )
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Surface,
    labelColor = TextSecondary,
    selectedContainerColor = PrimaryMuted,
    selectedLabelColor = Primary,
)

private val MARKET_CAP_OPTIONS = listOf(
    0L to "Any",
    1_000_000_000L to "$1B",
    10_000_000_000L to "$10B",
    50_000_000_000L to "$50B",
)

private val VOLUME_OPTIONS = listOf(
    0L to "Any",
    100_000_000L to "$100M",
    1_000_000_000L to "$1B",
    5_000_000_000L to "$5B",
)
