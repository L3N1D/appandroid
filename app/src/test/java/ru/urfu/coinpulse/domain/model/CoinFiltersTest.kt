package ru.urfu.coinpulse.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoinFiltersTest {

    @Test
    fun `default filters accept every coin`() {
        val coin = coin(marketCap = 1_000L, volume = 1_000L, change = -2.0)
        assertTrue(CoinFilters().matches(coin))
        assertFalse(CoinFilters().isActive)
    }

    @Test
    fun `market cap and volume thresholds are inclusive`() {
        val filters = CoinFilters(minMarketCap = 10_000L, minVolume24h = 5_000L)
        assertTrue(filters.isActive)
        assertTrue(filters.matches(coin(marketCap = 10_000L, volume = 5_000L, change = 1.0)))
        assertFalse(filters.matches(coin(marketCap = 9_999L, volume = 5_000L, change = 1.0)))
        assertFalse(filters.matches(coin(marketCap = 10_000L, volume = 4_999L, change = 1.0)))
    }

    @Test
    fun `performance filter separates gainers and losers`() {
        val gainer = coin(marketCap = 1L, volume = 1L, change = 1.0)
        val loser = coin(marketCap = 1L, volume = 1L, change = -1.0)

        assertTrue(CoinFilters(performance = PerformanceFilter.GAINERS).matches(gainer))
        assertFalse(CoinFilters(performance = PerformanceFilter.GAINERS).matches(loser))
        assertTrue(CoinFilters(performance = PerformanceFilter.LOSERS).matches(loser))
        assertFalse(CoinFilters(performance = PerformanceFilter.LOSERS).matches(gainer))
        assertTrue(CoinFilters(performance = PerformanceFilter.ALL).isActive.not())
    }

    private fun coin(
        marketCap: Long,
        volume: Long,
        change: Double,
    ) = Coin(
        id = "coin",
        rank = 1,
        symbol = "C",
        name = "Coin",
        imageUrl = "",
        price = 1.0,
        change24h = change,
        change7d = 0.0,
        marketCap = marketCap,
        volume24h = volume,
        high24h = 1.0,
        low24h = 1.0,
        circulatingSupply = 1.0,
        maxSupply = null,
        ath = 1.0,
        atl = 1.0,
        sparkline = emptyList(),
    )
}
