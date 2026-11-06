package ru.urfu.coinpulse.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails
import ru.urfu.coinpulse.domain.model.CoinPage
import ru.urfu.coinpulse.domain.model.CoinSort

/**
 * Domain contract. Presentation talks only to this interface through use cases.
 * The data layer decides whether a result comes from the network or the cache.
 */
interface CoinRepository {

    val favorites: StateFlow<List<Coin>>

    suspend fun getCoins(
        query: String,
        sort: CoinSort,
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean,
    ): Result<CoinPage>

    suspend fun getCoinDetails(
        id: String,
        forceRefresh: Boolean,
    ): Result<CoinDetails>

    suspend fun getChart(
        id: String,
        range: ChartRange,
    ): Result<List<Double>>

    fun toggleFavorite(coin: Coin)

    fun isFavorite(id: String): Boolean
}
