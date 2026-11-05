package ru.urfu.coinpulse.data

import kotlinx.coroutines.flow.StateFlow

const val DEFAULT_PAGE_SIZE = 10

/**
 * Contract the UI depends on. In the next practice the mock implementation can be
 * replaced by a Retrofit-backed one without touching the ViewModels or screens.
 */
interface CoinRepository {

    val favoriteIds: StateFlow<Set<String>>

    suspend fun getCoins(
        query: String = "",
        sort: CoinSort = CoinSort.MARKET_CAP,
        page: Int = 0,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): CoinPage

    suspend fun getCoinDetails(id: String): CoinDetails?

    suspend fun getFavoriteCoins(): List<Coin>

    fun getChartPoints(coin: Coin, range: ChartRange): List<Double>

    fun toggleFavorite(id: String)
}
