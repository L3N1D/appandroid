package ru.urfu.coinpulse.data.repository

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.urfu.coinpulse.data.local.FavoriteCoinDao
import ru.urfu.coinpulse.data.local.toFavoriteEntity
import ru.urfu.coinpulse.data.local.toDomain as toDomainEntity
import ru.urfu.coinpulse.data.remote.CoinGeckoApi
import ru.urfu.coinpulse.data.remote.mapper.toDomain
import ru.urfu.coinpulse.data.remote.mapper.toPricePoints
import ru.urfu.coinpulse.domain.error.AppException
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.CoinPage
import ru.urfu.coinpulse.domain.model.CoinSort
import ru.urfu.coinpulse.domain.repository.CoinRepository

class CoinRepositoryImpl(
    private val api: CoinGeckoApi,
    private val favoriteDao: FavoriteCoinDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
) : CoinRepository {

    override val favorites: Flow<List<Coin>> = favoriteDao.observeAll()
        .map { entities -> entities.map { it.toDomainEntity() } }

    private val marketCache = MutableStateFlow<CachedMarket?>(null)
    private val detailsCache = MutableStateFlow<Map<String, CachedDetails>>(emptyMap())
    private val chartCache = MutableStateFlow<Map<String, CachedChart>>(emptyMap())

    override suspend fun getCoins(
        query: String,
        sort: CoinSort,
        filters: CoinFilters,
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean,
    ): Result<CoinPage> = withContext(ioDispatcher) {
        try {
            val snapshot = loadMarketSnapshot(forceRefresh)
            syncFavorites(snapshot)

            val normalizedQuery = query.trim()
            val filtered = snapshot.filter { coin ->
                filters.matches(coin) && (
                    normalizedQuery.isEmpty() ||
                        coin.name.contains(normalizedQuery, ignoreCase = true) ||
                        coin.symbol.contains(normalizedQuery, ignoreCase = true) ||
                        coin.id.contains(normalizedQuery, ignoreCase = true)
                    )
            }.sortedWith(sort.comparator())

            val from = (page * pageSize).coerceAtLeast(0)
            val to = (from + pageSize).coerceAtMost(filtered.size)
            val items = if (from >= filtered.size) emptyList() else filtered.subList(from, to)

            Result.success(
                CoinPage(
                    items = items.toList(),
                    hasMore = to < filtered.size,
                    total = filtered.size,
                ),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(throwable.toAppException())
        }
    }

    override suspend fun getCoinDetails(
        id: String,
        forceRefresh: Boolean,
    ): Result<CoinDetails> = withContext(ioDispatcher) {
        try {
            val cached = detailsCache.value[id]
            if (!forceRefresh && cached != null && clock() - cached.timestamp < DETAILS_TTL_MS) {
                return@withContext Result.success(cached.details)
            }

            val fallbackCoin = marketCache.value?.coins?.firstOrNull { it.id == id }
            val response = api.getCoinDetails(
                id = id,
                localization = false,
                tickers = false,
                marketData = true,
                communityData = false,
                developerData = false,
                sparkline = true,
                cacheControl = if (forceRefresh) "no-cache" else null,
            )
            val details = response.toDomain(fallbackCoin)
                ?: throw AppException.EmptyResponse()

            detailsCache.value = detailsCache.value + (id to CachedDetails(details, clock()))
            syncFavorites(listOf(details.coin))
            Result.success(details)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(throwable.toAppException())
        }
    }

    override suspend fun getChart(
        id: String,
        range: ChartRange,
    ): Result<List<Double>> = withContext(ioDispatcher) {
        try {
            val detailsSparkline = detailsCache.value[id]?.details?.coin?.sparkline.orEmpty()
            if (range == ChartRange.WEEK && detailsSparkline.isNotEmpty()) {
                return@withContext Result.success(detailsSparkline)
            }
            if (range == ChartRange.DAY && detailsSparkline.size >= 24) {
                return@withContext Result.success(detailsSparkline.takeLast(24))
            }

            val key = "$id:${range.days}"
            val cached = chartCache.value[key]
            if (cached != null && clock() - cached.timestamp < CHART_TTL_MS) {
                return@withContext Result.success(cached.points)
            }

            val points = api.getMarketChart(
                id = id,
                vsCurrency = "usd",
                days = range.days,
                cacheControl = null,
            ).toPricePoints()
            if (points.isEmpty()) throw AppException.EmptyResponse()

            chartCache.value = chartCache.value + (key to CachedChart(points, clock()))
            Result.success(points)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(throwable.toAppException())
        }
    }

    override suspend fun toggleFavorite(coin: Coin) = withContext(ioDispatcher) {
        if (favoriteDao.exists(coin.id)) {
            favoriteDao.delete(coin.id)
        } else {
            favoriteDao.insert(coin.toFavoriteEntity())
        }
    }

    private suspend fun syncFavorites(coins: List<Coin>) {
        if (coins.isEmpty()) return
        val favoriteIds = favoriteDao.getFavoriteIds().toSet()
        if (favoriteIds.isEmpty()) return
        coins.filter { it.id in favoriteIds }.forEach { coin ->
            favoriteDao.insert(coin.toFavoriteEntity())
        }
    }

    private suspend fun loadMarketSnapshot(forceRefresh: Boolean): List<Coin> {
        val cached = marketCache.value
        if (!forceRefresh && cached != null && clock() - cached.timestamp < MARKET_TTL_MS) {
            return cached.coins
        }

        val response = api.getMarkets(
            vsCurrency = "usd",
            order = "market_cap_desc",
            perPage = MARKET_SNAPSHOT_SIZE,
            page = 1,
            sparkline = true,
            priceChangePercentage = "24h,7d",
            cacheControl = if (forceRefresh) "no-cache" else null,
        )
        val coins = response.mapIndexedNotNull { index, dto ->
            dto.toDomain(fallbackRank = index + 1)
        }
        if (coins.isEmpty()) throw AppException.EmptyResponse()

        marketCache.value = CachedMarket(coins = coins, timestamp = clock())
        return coins
    }

    private fun CoinSort.comparator(): Comparator<Coin> = when (this) {
        CoinSort.MARKET_CAP -> compareByDescending<Coin> { it.marketCap }.thenBy { it.rank }
        CoinSort.PRICE -> compareByDescending<Coin> { it.price }
        CoinSort.CHANGE_24H -> compareByDescending<Coin> { it.change24h }
    }

    private fun Throwable.toAppException(): AppException = when (this) {
        is AppException -> this
        is HttpException -> when {
            code() == 429 -> AppException.RateLimited()
            code() in 500..599 -> AppException.Server(code())
            else -> AppException.Server(code())
        }
        is IOException -> AppException.NoConnection(this)
        else -> AppException.Unknown(this)
    }

    private data class CachedMarket(
        val coins: List<Coin>,
        val timestamp: Long,
    )

    private data class CachedDetails(
        val details: CoinDetails,
        val timestamp: Long,
    )

    private data class CachedChart(
        val points: List<Double>,
        val timestamp: Long,
    )

    companion object {
        private const val MARKET_SNAPSHOT_SIZE = 30
        private const val MARKET_TTL_MS = 55_000L
        private const val DETAILS_TTL_MS = 5 * 60_000L
        private const val CHART_TTL_MS = 5 * 60_000L
    }
}
