package ru.urfu.coinpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.urfu.coinpulse.ui.format.asPercent
import ru.urfu.coinpulse.ui.theme.Negative
import ru.urfu.coinpulse.ui.theme.Positive

@Composable
fun ChangeBadge(
    percent: Double,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val color = changeColor(percent)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.13f))
            .padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 2.dp else 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(if (compact) 14.dp else 18.dp)
                .rotate(if (percent >= 0) 180f else 0f),
        )
        Text(
            text = percent.asPercent(),
            color = color,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
        )
    }
}

fun changeColor(percent: Double): Color = if (percent >= 0) Positive else Negative
