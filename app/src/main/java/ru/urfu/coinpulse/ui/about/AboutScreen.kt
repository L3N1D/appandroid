package ru.urfu.coinpulse.ui.about

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.urfu.coinpulse.ui.theme.Divider
import ru.urfu.coinpulse.ui.theme.Primary
import ru.urfu.coinpulse.ui.theme.PrimaryMuted
import ru.urfu.coinpulse.ui.theme.Surface
import ru.urfu.coinpulse.ui.theme.SurfaceHighest
import ru.urfu.coinpulse.ui.theme.TextPrimary
import ru.urfu.coinpulse.ui.theme.TextSecondary
import ru.urfu.coinpulse.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Text(
                text = "Practice 3 · single-activity crypto explorer",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryMuted, SurfaceHighest),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "₿",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                }
                Spacer(Modifier.padding(start = 8.dp))
                Column {
                    Text(
                        text = "CoinPulse",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = "v1.0 · Jetpack Compose",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                }
            }
        }

        SectionCard(title = "What is implemented") {
            FeatureRow("Single activity, four Compose destinations")
            FeatureRow("Bottom navigation with saved/restored tab state")
            FeatureRow("Paged LazyColumn with search and sorting")
            FeatureRow("Search debounce and pull-to-refresh")
            FeatureRow("Details screen with a ConstraintLayout stats grid")
            FeatureRow("Watchlist shared between list, details and tab")
            FeatureRow("MVVM with StateFlow and SavedStateHandle navigation args")
        }

        SectionCard(title = "CoinGecko endpoints (mocked)") {
            Text(
                text = "The project prepares models and UI for the public CoinGecko API. Nothing is fetched in this practice — all values come from a deterministic in-memory repository.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(10.dp))
            EndpointBox("GET /coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1")
            Spacer(Modifier.height(7.dp))
            EndpointBox("GET /coins/{id}?localization=false&tickers=false&market_data=false")
            Spacer(Modifier.height(7.dp))
            EndpointBox("GET /coins/{id}/market_chart?vs_currency=usd&days=7")
        }

        SectionCard(title = "Tech stack") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Kotlin",
                    "Jetpack Compose",
                    "Material 3",
                    "Navigation Compose",
                    "ViewModel",
                    "StateFlow",
                    "Coroutines",
                    "Coil",
                    "ConstraintLayout",
                ).forEach { TechChip(it) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceHighest.copy(alpha = 0.6f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.padding(start = 6.dp))
            Text(
                text = "Next practice: replace the mock repository with a network data source and persist settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Spacer(Modifier.padding(start = 6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
private fun EndpointBox(path: String) {
    Text(
        text = path,
        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
        color = Primary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceHighest)
            .padding(horizontal = 10.dp, vertical = 9.dp),
    )
}

@Composable
private fun TechChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceHighest)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
