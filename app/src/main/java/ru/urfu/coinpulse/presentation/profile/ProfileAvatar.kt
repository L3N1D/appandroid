package ru.urfu.coinpulse.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import ru.urfu.coinpulse.presentation.format.initials
import ru.urfu.coinpulse.presentation.theme.Primary
import ru.urfu.coinpulse.presentation.theme.PrimaryMuted
import ru.urfu.coinpulse.presentation.theme.SurfaceHighest
import ru.urfu.coinpulse.presentation.theme.TextPrimary

@Composable
fun ProfileAvatar(
    avatarUri: String,
    fullName: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    if (avatarUri.isBlank()) {
        AvatarFallback(fullName = fullName, size = size, modifier = modifier)
        return
    }

    val context = LocalContext.current
    val request = remember(avatarUri) {
        ImageRequest.Builder(context)
            .data(avatarUri)
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = "Profile photo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceHighest),
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            else -> AvatarFallback(fullName = fullName, size = size)
        }
    }
}

@Composable
private fun AvatarFallback(
    fullName: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(PrimaryMuted, SurfaceHighest)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (fullName.isBlank()) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(size * 0.42f),
            )
        } else {
            Text(
                text = fullName.initials(),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.3f).sp,
            )
        }
    }
}
