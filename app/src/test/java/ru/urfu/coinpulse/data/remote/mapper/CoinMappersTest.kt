package ru.urfu.coinpulse.data.remote.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.urfu.coinpulse.data.remote.dto.DescriptionDto
import ru.urfu.coinpulse.data.remote.dto.ImageDto
import ru.urfu.coinpulse.data.remote.dto.LinksDto
import ru.urfu.coinpulse.data.remote.dto.MarketChartDto
import ru.urfu.coinpulse.data.remote.dto.MarketCoinDto
import ru.urfu.coinpulse.data.remote.dto.MarketDataDto
import ru.urfu.coinpulse.data.remote.dto.ReposUrlDto
import ru.urfu.coinpulse.data.remote.dto.SparklineDto
import ru.urfu.coinpulse.data.remote.dto.CoinDetailsDto

class CoinMappersTest {

    @Test
    fun `market dto maps nullable numeric fields to safe defaults`() {
        val dto = MarketCoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "https://example.com/btc.png",
            sparklineIn7d = SparklineDto(listOf(1.0, 2.0, 3.0)),
        )

        val coin = dto.toDomain(fallbackRank = 7)

        assertEquals("bitcoin", coin?.id)
        assertEquals("BTC", coin?.symbol)
        assertEquals(7, coin?.rank)
        assertEquals(0.0, coin?.price ?: -1.0, 0.0)
        assertEquals(3, coin?.sparkline?.size)
    }

    @Test
    fun `market dto without id is ignored`() {
        assertNull(MarketCoinDto(name = "Broken").toDomain(fallbackRank = 1))
    }

    @Test
    fun `details dto strips html and maps links and market data`() {
        val dto = CoinDetailsDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = ImageDto(large = "https://example.com/btc.png"),
            description = DescriptionDto(en = "<p>Bitcoin is <b>digital gold</b> &amp; more.</p>"),
            links = LinksDto(
                homepage = listOf("", "https://bitcoin.org"),
                reposUrl = ReposUrlDto(listOf("https://github.com/bitcoin/bitcoin")),
                subredditUrl = "https://reddit.com/r/bitcoin",
            ),
            categories = listOf("Layer 1", "Store of value"),
            genesisDate = "2009-01-03",
            hashingAlgorithm = "SHA-256",
            marketCapRank = 1,
            marketData = MarketDataDto(
                currentPrice = mapOf("usd" to 100.0),
                marketCap = mapOf("usd" to 1_000_000.0),
                totalVolume = mapOf("usd" to 50_000.0),
                high24h = mapOf("usd" to 110.0),
                low24h = mapOf("usd" to 90.0),
                priceChange24h = 1.5,
                priceChange7d = 2.5,
                priceChange30d = 3.5,
                priceChange60d = 4.5,
                priceChange1y = 5.5,
                circulatingSupply = 1000.0,
                totalSupply = 2000.0,
                maxSupply = 2100.0,
                ath = mapOf("usd" to 200.0),
                atl = mapOf("usd" to 10.0),
                fullyDilutedValuation = mapOf("usd" to 1_100_000.0),
                sparkline7d = SparklineDto(listOf(1.0, 2.0)),
            ),
            lastUpdated = "2026-11-06T12:00:00.000Z",
        )

        val details = dto.toDomain()

        assertEquals("Bitcoin is digital gold & more.", details?.description)
        assertEquals("https://bitcoin.org", details?.homepage)
        assertEquals("https://github.com/bitcoin/bitcoin", details?.githubUrl)
        assertEquals(2, details?.categories?.size)
        assertEquals(3.5, details?.change30d ?: 0.0, 0.0)
        assertEquals(1_100_000L, details?.fullyDilutedValuation)
        assertEquals(100.0, details?.coin?.price ?: 0.0, 0.0)
        assertTrue(details?.lastUpdated?.contains("2026") == true)
    }

    @Test
    fun `chart dto keeps only valid price points`() {
        val dto = MarketChartDto(
            prices = listOf(
                listOf(1.0, 10.0),
                listOf(2.0),
                listOf(3.0, 0.0),
                listOf(4.0, 40.0),
            ),
        )

        assertEquals(listOf(10.0, 40.0), dto.toPricePoints())
    }
}
