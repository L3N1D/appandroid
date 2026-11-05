package ru.urfu.coinpulse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.urfu.coinpulse.data.Coin
import ru.urfu.coinpulse.ui.format.asCompactCurrency
import ru.urfu.coinpulse.ui.format.asPercent
import ru.urfu.coinpulse.ui.format.asPrice
import ru.urfu.coinpulse.ui.theme.Divider
import ru.urfu.coinpulse.ui.theme.Negative
import ru.urfu.coinpulse.ui.theme.Positive
import ru.urfu.coinpulse.ui.theme.Surface
import ru.urfu.coinpulse.ui.theme.TextPrimary
import ru.urfu.coinpulse.ui.theme.TextSecondary
import ru.urfu.coinpulse.ui.theme.TextTertiary
import ru.urfu.coinpulse.ui.theme.Warning

@Composable
fun CoinRow(
    coin: Coin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Divider.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = if (onFavoriteClick != null) 2.dp else 14.dp, top = 11.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#${coin.rank}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                modifier = Modifier.width(28.dp),
            )
            CoinLogo(coin = coin, size = 40.dp)
            Spacer(Modifier.width(11.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${coin.symbol} · ${coin.marketCap.asCompactCurrency()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Sparkline(
                points = coin.sparkline,
                positive = coin.isPositive24h,
                modifier = Modifier
                    .width(54.dp)
                    .size(width = 54.dp, height = 30.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = coin.price.asPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = coin.change24h.asPercent(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (coin.isPositive24h) Positive else Negative,
                    maxLines = 1,
                )
            }
            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from watchlist" else "Add to watchlist",
                        tint = if (isFavorite) Warning else TextTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
