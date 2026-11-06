package ru.urfu.coinpulse.domain.model

enum class PerformanceFilter(val label: String) {
    ALL("All"),
    GAINERS("Gainers"),
    LOSERS("Losers"),
}

/**
 * Up to three user-configurable market filters. Persisted in DataStore and applied
 * to the cached market snapshot by the repository.
 */
data class CoinFilters(
    val minMarketCap: Long = 0L,
    val performance: PerformanceFilter = PerformanceFilter.ALL,
    val minVolume24h: Long = 0L,
) {
    val isActive: Boolean
        get() = minMarketCap > 0L ||
            performance != PerformanceFilter.ALL ||
            minVolume24h > 0L

    fun matches(coin: Coin): Boolean {
        if (coin.marketCap < minMarketCap) return false
        if (coin.volume24h < minVolume24h) return false
        return when (performance) {
            PerformanceFilter.ALL -> true
            PerformanceFilter.GAINERS -> coin.change24h > 0.0
            PerformanceFilter.LOSERS -> coin.change24h < 0.0
        }
    }
}
