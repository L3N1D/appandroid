package ru.urfu.coinpulse.presentation.about

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.ApplicationInfo
import com.chuckerteam.chucker.api.Chucker
import ru.urfu.coinpulse.presentation.theme.Divider
import ru.urfu.coinpulse.presentation.theme.Primary
import ru.urfu.coinpulse.presentation.theme.PrimaryMuted
import ru.urfu.coinpulse.presentation.theme.Surface
import ru.urfu.coinpulse.presentation.theme.SurfaceHighest
import ru.urfu.coinpulse.presentation.theme.TextPrimary
import ru.urfu.coinpulse.presentation.theme.TextSecondary
import ru.urfu.coinpulse.presentation.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

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
                text = "Practice 6 · profile & local files",
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
            FeatureRow("Single activity, six Compose destinations")
            FeatureRow("Bottom navigation with saved/restored tab state")
            FeatureRow("Live CoinGecko data via Retrofit + OkHttp")
            FeatureRow("Domain, data and presentation layers with use cases")
            FeatureRow("Loading, error and retry states for every request")
            FeatureRow("Paged list, search, sorting and pull-to-refresh")
            FeatureRow("Filters screen persisted with DataStore")
            FeatureRow("Offline watchlist stored in Room")
            FeatureRow("Shared filter badge cache created via DI")
            FeatureRow("Profile with gallery/camera avatar and resume download")
            FeatureRow("Details screen with a ConstraintLayout stats grid")
        }

        SectionCard(title = "Local storage") {
            FeatureRow("Filters: DataStore Preferences, 3 settings, restored on launch")
            FeatureRow("Favorites: Room database, available without internet")
            FeatureRow("Badge: shared FilterBadgeCache instance created by AppContainer")
            FeatureRow("Profile: DataStore fields + avatar copied into internal storage")
            FeatureRow("Resume: downloaded with OkHttp and opened via FileProvider")
            Text(
                text = "Gallery uses the system Photo Picker and camera uses ACTION_IMAGE_CAPTURE with FileProvider, so no storage or camera runtime permission is requested.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        SectionCard(title = "Live CoinGecko API") {
            Text(
                text = "The app now loads real data from the public keyless CoinGecko API. Requests are executed on Dispatchers.IO through the repository and use cases; the UI renders loading, content and error states.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(10.dp))
            EndpointBox("GET /coins/markets?vs_currency=usd&order=market_cap_desc&per_page=30&page=1&sparkline=true")
            Spacer(Modifier.height(7.dp))
            EndpointBox("GET /coins/{id}?localization=false&market_data=true&sparkline=true")
            Spacer(Modifier.height(7.dp))
            EndpointBox("GET /coins/{id}/market_chart?vs_currency=usd&days=30")
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
                    "StateFlow / Flow",
                    "Coroutines",
                    "Retrofit",
                    "OkHttp",
                    "Gson",
                    "DataStore",
                    "Room / KSP",
                    "FileProvider",
                    "Photo Picker",
                    "UseCases",
                    "Clean Architecture",
                    "Chucker",
                    "Coil",
                    "ConstraintLayout",
                ).forEach { TechChip(it) }
            }
        }

        if (isDebuggable) {
            SectionCard(title = "Network inspector") {
                Text(
                    text = "Debug builds record every request in Chucker. Open it to show the market, details and chart calls for the practice report.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Chucker.getLaunchIntent(context))
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Divider),
                ) {
                    Text(
                        text = "Open Chucker",
                        color = Primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
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
                text = "Profile fields and avatars stay on the device; the resume is downloaded on demand.",
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
