package ru.urfu.coinpulse.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import ru.urfu.coinpulse.data.local.FavoriteCoinDao
import ru.urfu.coinpulse.data.local.FavoriteCoinEntity
import ru.urfu.coinpulse.data.remote.CoinGeckoApi
import ru.urfu.coinpulse.data.remote.dto.CoinDetailsDto
import ru.urfu.coinpulse.data.remote.dto.DescriptionDto
import ru.urfu.coinpulse.data.remote.dto.ImageDto
import ru.urfu.coinpulse.data.remote.dto.LinksDto
import ru.urfu.coinpulse.data.remote.dto.MarketChartDto
import ru.urfu.coinpulse.data.remote.dto.MarketCoinDto
import ru.urfu.coinpulse.data.remote.dto.MarketDataDto
import ru.urfu.coinpulse.data.remote.dto.ReposUrlDto
import ru.urfu.coinpulse.data.remote.dto.SparklineDto
import ru.urfu.coinpulse.domain.error.AppException
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.CoinSort
import ru.urfu.coinpulse.domain.model.PerformanceFilter
import java.io.IOException

class CoinRepositoryImplTest {

    @Test
    fun `market request uses expected query parameters and paginates`() = runTest {
        val api = FakeApi().apply {
            marketResponse = (1..12).map { index ->
                marketDto(
                    id = "coin$index",
                    name = "Coin $index",
                    price = 100.0 + index,
                    rank = index,
                    marketCap = 1_000_000.0 - index,
                )
            }
        }
        val repository = repository(api)

        val firstPage = repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false).getOrThrow()
        val secondPage = repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 1, 10, false).getOrThrow()

        assertEquals(10, firstPage.items.size)
        assertTrue(firstPage.hasMore)
        assertEquals(12, firstPage.total)
        assertEquals(2, secondPage.items.size)
        assertFalse(secondPage.hasMore)
        assertEquals(30, api.lastPerPage)
        assertEquals(1, api.lastPage)
        assertEquals("market_cap_desc", api.lastOrder)
        assertEquals(true, api.lastSparkline)
        assertEquals(1, api.marketsCalls)
    }

    @Test
    fun `search and sorting run over the cached snapshot`() = runTest {
        val api = FakeApi().apply {
            marketResponse = listOf(
                marketDto("bitcoin", "Bitcoin", 100.0, 1, 3_000.0),
                marketDto("bitcoin-cash", "Bitcoin Cash", 50.0, 2, 2_000.0),
                marketDto("ethereum", "Ethereum", 200.0, 3, 1_000.0),
            )
        }
        val repository = repository(api)

        val search = repository.getCoins("bitcoin", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false).getOrThrow()
        val priceSorted = repository.getCoins("", CoinSort.PRICE, CoinFilters(), 0, 10, false).getOrThrow()

        assertEquals(2, search.total)
        assertEquals("ethereum", priceSorted.items.first().id)
    }

    @Test
    fun `filters are applied to market cap volume and 24h performance`() = runTest {
        val api = FakeApi().apply {
            marketResponse = listOf(
                marketDto("big-gainer", "Big Gainer", 100.0, 1, 20_000.0, volume = 2_000.0, change24h = 4.0),
                marketDto("small-gainer", "Small Gainer", 10.0, 2, 1_000.0, volume = 2_000.0, change24h = 8.0),
                marketDto("big-loser", "Big Loser", 80.0, 3, 15_000.0, volume = 2_000.0, change24h = -3.0),
                marketDto("low-volume", "Low Volume", 90.0, 4, 18_000.0, volume = 10.0, change24h = 5.0),
            )
        }
        val repository = repository(api)

        val result = repository.getCoins(
            query = "",
            sort = CoinSort.MARKET_CAP,
            filters = CoinFilters(
                minMarketCap = 10_000L,
                performance = PerformanceFilter.GAINERS,
                minVolume24h = 1_000L,
            ),
            page = 0,
            pageSize = 10,
            forceRefresh = false,
        ).getOrThrow()

        assertEquals(1, result.total)
        assertEquals("big-gainer", result.items.first().id)
    }

    @Test
    fun `market snapshot is cached until its TTL expires`() = runTest {
        var now = 0L
        val api = FakeApi().apply {
            marketResponse = listOf(marketDto("bitcoin", "Bitcoin", 100.0, 1, 1_000.0))
        }
        val repository = repository(api, clock = { now })

        repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)
        repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)
        assertEquals(1, api.marketsCalls)

        now = 60_000L
        repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)
        assertEquals(2, api.marketsCalls)
    }

    @Test
    fun `force refresh bypasses the market cache`() = runTest {
        val api = FakeApi().apply {
            marketResponse = listOf(marketDto("bitcoin", "Bitcoin", 100.0, 1, 1_000.0))
        }
        val repository = repository(api)

        repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, true)
        repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, true)

        assertEquals(2, api.marketsCalls)
    }

    @Test
    fun `network IOException is mapped to NoConnection`() = runTest {
        val api = FakeApi().apply {
            failure = IOException("offline")
        }
        val repository = repository(api)

        val result = repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)

        assertTrue(result.exceptionOrNull() is AppException.NoConnection)
    }

    @Test
    fun `HTTP 429 is mapped to RateLimited`() = runTest {
        val api = FakeApi().apply {
            failure = HttpException(
                Response.error<Any>(429, "rate limited".toResponseBody()),
            )
        }
        val repository = repository(api)

        val result = repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)

        assertTrue(result.exceptionOrNull() is AppException.RateLimited)
    }

    @Test
    fun `details are mapped and cached`() = runTest {
        val api = FakeApi().apply {
            marketResponse = listOf(marketDto("bitcoin", "Bitcoin", 100.0, 1, 1_000.0))
            detailsResponse = detailsDto()
        }
        val repository = repository(api)

        val first = repository.getCoinDetails("bitcoin", false).getOrThrow()
        val second = repository.getCoinDetails("bitcoin", false).getOrThrow()

        assertEquals("Bitcoin", first.coin.name)
        assertEquals("Bitcoin is digital gold & more.", first.description)
        assertEquals("https://bitcoin.org", first.homepage)
        assertEquals(1, api.detailsCalls)
        assertEquals(first.coin.id, second.coin.id)
    }

    @Test
    fun `chart uses details sparkline for week and day and fetches longer ranges`() = runTest {
        val api = FakeApi().apply {
            detailsResponse = detailsDto()
            chartResponse = MarketChartDto(listOf(listOf(1.0, 10.0), listOf(2.0, 20.0)))
        }
        val repository = repository(api)
        repository.getCoinDetails("bitcoin", false).getOrThrow()

        val week = repository.getChart("bitcoin", ChartRange.WEEK).getOrThrow()
        val day = repository.getChart("bitcoin", ChartRange.DAY).getOrThrow()
        val month = repository.getChart("bitcoin", ChartRange.MONTH).getOrThrow()

        assertEquals(168, week.size)
        assertEquals(24, day.size)
        assertEquals(listOf(10.0, 20.0), month)
        assertEquals(1, api.chartCalls)
    }

    @Test
    fun `favorites are persisted through the dao and survive a new repository instance`() = runTest {
        val api = FakeApi().apply {
            marketResponse = listOf(marketDto("bitcoin", "Bitcoin", 100.0, 1, 1_000.0))
        }
        val dao = FakeFavoriteDao()
        val repository = repository(api, dao)
        val coin = repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)
            .getOrThrow()
            .items
            .first()

        repository.toggleFavorite(coin)
        val favorites = repository.favorites.first()
        assertEquals(1, favorites.size)
        assertEquals("Bitcoin", favorites.first().name)

        // Simulates a new process / ViewModel using the same Room storage.
        val newRepository = repository(api, dao)
        assertEquals(1, newRepository.favorites.first().size)

        newRepository.toggleFavorite(coin)
        assertTrue(newRepository.favorites.first().isEmpty())
    }

    @Test
    fun `favorite snapshots are refreshed when the market loads again`() = runTest {
        val api = FakeApi().apply {
            marketResponse = listOf(marketDto("bitcoin", "Bitcoin", 100.0, 1, 1_000.0))
        }
        val repository = repository(api)
        val coin = repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, false)
            .getOrThrow()
            .items
            .first()
        repository.toggleFavorite(coin)

        api.marketResponse = listOf(marketDto("bitcoin", "Bitcoin", 123.0, 1, 1_000.0))
        repository.getCoins("", CoinSort.MARKET_CAP, CoinFilters(), 0, 10, true)

        assertEquals(123.0, repository.favorites.first().first().price, 0.0)
    }

    private fun repository(
        api: FakeApi,
        dao: FakeFavoriteDao = FakeFavoriteDao(),
        clock: () -> Long = { 0L },
    ) = CoinRepositoryImpl(
        api = api,
        favoriteDao = dao,
        ioDispatcher = Dispatchers.Unconfined,
        clock = clock,
    )

    private fun marketDto(
        id: String,
        name: String,
        price: Double,
        rank: Int,
        marketCap: Double,
        volume: Double = 50_000.0,
        change24h: Double = 1.5,
    ) = MarketCoinDto(
        id = id,
        symbol = id.take(3),
        name = name,
        image = "https://example.com/$id.png",
        currentPrice = price,
        marketCap = marketCap,
        marketCapRank = rank,
        totalVolume = volume,
        high24h = price * 1.1,
        low24h = price * 0.9,
        priceChange24h = change24h,
        priceChange7dInCurrency = 2.5,
        circulatingSupply = 1_000.0,
        maxSupply = 2_000.0,
        ath = price * 2,
        atl = price / 2,
        sparklineIn7d = SparklineDto(List(168) { price + it }),
    )

    private fun detailsDto() = CoinDetailsDto(
        id = "bitcoin",
        symbol = "btc",
        name = "Bitcoin",
        image = ImageDto(large = "https://example.com/btc.png"),
        description = DescriptionDto(en = "<p>Bitcoin is <b>digital gold</b> &amp; more.</p>"),
        links = LinksDto(
            homepage = listOf("https://bitcoin.org"),
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
            circulatingSupply = 1_000.0,
            totalSupply = 2_000.0,
            maxSupply = 2_100.0,
            ath = mapOf("usd" to 200.0),
            atl = mapOf("usd" to 10.0),
            fullyDilutedValuation = mapOf("usd" to 1_100_000.0),
            sparkline7d = SparklineDto(List(168) { 100.0 + it }),
        ),
        lastUpdated = "2026-11-06T12:00:00.000Z",
    )

    private class FakeFavoriteDao : FavoriteCoinDao {
        private val entities = MutableStateFlow<Map<String, FavoriteCoinEntity>>(emptyMap())

        override fun observeAll(): Flow<List<FavoriteCoinEntity>> = entities.map { values ->
            values.values.sortedBy { it.rank }
        }

        override suspend fun getFavoriteIds(): List<String> = entities.value.keys.toList()

        override suspend fun exists(id: String): Boolean = id in entities.value

        override suspend fun insert(entity: FavoriteCoinEntity) {
            entities.update { it + (entity.id to entity) }
        }

        override suspend fun delete(id: String) {
            entities.update { it - id }
        }

        override suspend fun clear() {
            entities.value = emptyMap()
        }
    }

    private class FakeApi : CoinGeckoApi {
        var marketsCalls = 0
        var detailsCalls = 0
        var chartCalls = 0
        var failure: Throwable? = null
        var marketResponse: List<MarketCoinDto> = emptyList()
        var detailsResponse: CoinDetailsDto = CoinDetailsDto()
        var chartResponse: MarketChartDto = MarketChartDto()
        var lastPerPage: Int? = null
        var lastPage: Int? = null
        var lastOrder: String? = null
        var lastSparkline: Boolean? = null

        override suspend fun getMarkets(
            vsCurrency: String,
            order: String,
            perPage: Int,
            page: Int,
            sparkline: Boolean,
            priceChangePercentage: String,
            cacheControl: String?,
        ): List<MarketCoinDto> {
            marketsCalls++
            lastPerPage = perPage
            lastPage = page
            lastOrder = order
            lastSparkline = sparkline
            failure?.let { throw it }
            return marketResponse
        }

        override suspend fun getCoinDetails(
            id: String,
            localization: Boolean,
            tickers: Boolean,
            marketData: Boolean,
            communityData: Boolean,
            developerData: Boolean,
            sparkline: Boolean,
            cacheControl: String?,
        ): CoinDetailsDto {
            detailsCalls++
            failure?.let { throw it }
            return detailsResponse
        }

        override suspend fun getMarketChart(
            id: String,
            vsCurrency: String,
            days: Int,
            cacheControl: String?,
        ): MarketChartDto {
            chartCalls++
            failure?.let { throw it }
            return chartResponse
        }
    }
}
