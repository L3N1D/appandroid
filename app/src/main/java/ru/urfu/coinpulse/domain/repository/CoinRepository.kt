package ru.urfu.coinpulse.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.CoinPage
import ru.urfu.coinpulse.domain.model.CoinSort

/**
 * Domain contract. Presentation talks only to this interface through use cases.
 * The data layer decides whether a result comes from the network or the cache.
 */
interface CoinRepository {

    val favorites: Flow<List<Coin>>

    suspend fun getCoins(
        query: String,
        sort: CoinSort,
        filters: CoinFilters,
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

    suspend fun toggleFavorite(coin: Coin)
}
