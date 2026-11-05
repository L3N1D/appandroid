package ru.urfu.coinpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import ru.urfu.coinpulse.data.Coin
import ru.urfu.coinpulse.ui.format.initials
import ru.urfu.coinpulse.ui.theme.CoinPalette
import ru.urfu.coinpulse.ui.theme.SurfaceElevated
import ru.urfu.coinpulse.ui.theme.TextPrimary

@Composable
fun CoinLogo(
    coin: Coin,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
) {
    val context = LocalContext.current
    val request = remember(coin.imageUrl) {
        ImageRequest.Builder(context)
            .data(coin.imageUrl)
            .crossfade(true)
            .build()
    }
    val palette = remember(coin.id) {
        val start = coin.id.hashCode().let { if (it < 0) -it else it } % CoinPalette.size
        CoinPalette[start] to CoinPalette[(start + 3) % CoinPalette.size]
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = "${coin.name} logo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceElevated),
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            else -> FallbackLogo(
                text = coin.name.initials(),
                colors = palette,
                size = size,
            )
        }
    }
}

@Composable
private fun FallbackLogo(
    text: String,
    colors: Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color>,
    size: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.first.copy(alpha = 0.85f),
                        colors.second.copy(alpha = 0.95f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.34f).sp,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
