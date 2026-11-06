package ru.urfu.coinpulse.presentation.format

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

private val usdFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    currency = Currency.getInstance("USD")
}

/** Prices keep more decimals as they get smaller ($0.00002418). */
fun Double.asPrice(): String = when {
    this >= 1 -> String.format(Locale.US, "$%,.2f", this)
    this >= 0.01 -> String.format(Locale.US, "$%,.4f", this)
    else -> String.format(Locale.US, "$%,.8f", this)
}

/** Fallback used where a currency-formatted string is required. */
fun Double.asCurrency(): String = usdFormat.format(this)

fun Long.asCompactCurrency(): String = when {
    this >= 1_000_000_000_000 -> String.format(Locale.US, "$%.2fT", this / 1_000_000_000_000.0)
    this >= 1_000_000_000 -> String.format(Locale.US, "$%.2fB", this / 1_000_000_000.0)
    this >= 1_000_000 -> String.format(Locale.US, "$%.2fM", this / 1_000_000.0)
    this >= 1_000 -> String.format(Locale.US, "$%.2fK", this / 1_000.0)
    else -> String.format(Locale.US, "$%,d", this)
}

fun Double.asCompactNumber(): String = when {
    this >= 1_000_000_000_000 -> String.format(Locale.US, "%.2fT", this / 1_000_000_000_000)
    this >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", this / 1_000_000_000)
    this >= 1_000_000 -> String.format(Locale.US, "%.2fM", this / 1_000_000)
    this >= 1_000 -> String.format(Locale.US, "%.2fK", this / 1_000)
    else -> String.format(Locale.US, "%,.2f", this)
}

fun Double.asPercent(): String {
    val sign = if (this > 0) "+" else if (this < 0) "−" else ""
    return String.format(Locale.US, "%s%.2f%%", sign, abs(this))
}

/** Percentage with the ASCII minus used in tight table cells. */
fun Double.asPercentAscii(): String = String.format(Locale.US, "%+.2f%%", this)

fun String.initials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        isNotBlank() -> take(1).uppercase()
        else -> "?"
    }
}
