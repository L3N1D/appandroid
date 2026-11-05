package ru.urfu.coinpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ru.urfu.coinpulse.ui.theme.Divider
import ru.urfu.coinpulse.ui.theme.Negative
import ru.urfu.coinpulse.ui.theme.Positive

/** Compact 7-day sparkline used inside list rows. */
@Composable
fun Sparkline(
    points: List<Double>,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return
    val color = if (positive) Positive else Negative

    Canvas(modifier = modifier) {
        val min = points.minOrNull() ?: return@Canvas
        val max = points.maxOrNull() ?: return@Canvas
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val inset = 1.5.dp.toPx()
        val height = (size.height - inset * 2).coerceAtLeast(1f)
        val stepX = size.width / (points.size - 1)

        val path = Path()
        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = inset + height - ((value - min) / range * height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/** Larger chart for the coin details screen with a gradient fill and a last-price dot. */
@Composable
fun PriceChart(
    points: List<Double>,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return
    val color = if (positive) Positive else Negative

    Canvas(modifier = modifier) {
        val min = points.minOrNull() ?: return@Canvas
        val max = points.maxOrNull() ?: return@Canvas
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val horizontalPadding = 4.dp.toPx()
        val verticalPadding = 10.dp.toPx()
        val chartWidth = (size.width - horizontalPadding * 2).coerceAtLeast(1f)
        val chartHeight = (size.height - verticalPadding * 2).coerceAtLeast(1f)
        val stepX = chartWidth / (points.size - 1)

        // Horizontal grid.
        repeat(4) { index ->
            val y = verticalPadding + chartHeight * (index / 3f)
            drawLine(
                color = Divider.copy(alpha = 0.75f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        fun xOf(index: Int) = horizontalPadding + index * stepX
        fun yOf(value: Double) =
            verticalPadding + chartHeight - ((value - min) / range * chartHeight).toFloat()

        val linePath = Path()
        points.forEachIndexed { index, value ->
            val x = xOf(index)
            val y = yOf(value)
            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(xOf(points.lastIndex), size.height)
            lineTo(xOf(0), size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.30f), Color.Transparent),
                startY = verticalPadding,
                endY = size.height,
            ),
        )
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        val lastX = xOf(points.lastIndex)
        val lastY = yOf(points.last())
        drawCircle(color = color.copy(alpha = 0.22f), radius = 7.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(color = color, radius = 3.2.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(color = Color(0xFF0B1421), radius = 1.3.dp.toPx(), center = Offset(lastX, lastY))
    }
}
