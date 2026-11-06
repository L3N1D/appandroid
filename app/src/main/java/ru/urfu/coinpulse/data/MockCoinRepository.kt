package ru.urfu.coinpulse.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory implementation of [CoinRepository]. It deliberately mimics a paged API:
 * each page is fetched with a small delay so loading placeholders and pagination can
 * be verified in the UI.
 */
class MockCoinRepository(
    private val simulateNetworkDelay: Boolean = true,
) : CoinRepository {

    private val coins: List<Coin> = MockCoinData.buildCoins()

    private val _favoriteIds = MutableStateFlow(setOf("bitcoin", "solana"))

    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    override suspend fun getCoins(
        query: String,
        sort: CoinSort,
        page: Int,
        pageSize: Int,
    ): CoinPage {
        if (simulateNetworkDelay) delay(650)

        val normalizedQuery = query.trim()
        val filtered = coins
            .asSequence()
            .filter { coin ->
                normalizedQuery.isEmpty() ||
                    coin.name.contains(normalizedQuery, ignoreCase = true) ||
                    coin.symbol.contains(normalizedQuery, ignoreCase = true) ||
                    coin.id.contains(normalizedQuery, ignoreCase = true)
            }
            .sortedWith(sort.comparator())
            .toList()

        val from = (page * pageSize).coerceAtLeast(0)
        val to = (from + pageSize).coerceAtMost(filtered.size)
        val items = if (from >= filtered.size) emptyList() else filtered.subList(from, to)

        return CoinPage(
            items = items.toList(),
            hasMore = to < filtered.size,
            total = filtered.size,
        )
    }

    override suspend fun getCoinDetails(id: String): CoinDetails? {
        if (simulateNetworkDelay) delay(450)
        val coin = coins.firstOrNull { it.id == id } ?: return null
        return MockCoinData.detailsFor(coin)
    }

    override suspend fun getFavoriteCoins(): List<Coin> {
        if (simulateNetworkDelay) delay(200)
        val favorites = _favoriteIds.value
        return coins.filter { it.id in favorites }.sortedBy { it.rank }
    }

    override fun getChartPoints(coin: Coin, range: ChartRange): List<Double> =
        MockCoinData.chartPoints(coin, range)

    override fun toggleFavorite(id: String) {
        _favoriteIds.value = _favoriteIds.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    private fun CoinSort.comparator(): Comparator<Coin> = when (this) {
        CoinSort.MARKET_CAP -> compareByDescending<Coin> { it.marketCap }.thenBy { it.rank }
        CoinSort.PRICE -> compareByDescending<Coin> { it.price }
        CoinSort.CHANGE_24H -> compareByDescending<Coin> { it.change24h }
    }
}
