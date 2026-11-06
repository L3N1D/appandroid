package ru.urfu.coinpulse.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails
import ru.urfu.coinpulse.domain.model.CoinPage
import ru.urfu.coinpulse.domain.model.CoinSort
import ru.urfu.coinpulse.domain.repository.CoinRepository

class GetCoinsUseCase(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(
        query: String,
        sort: CoinSort,
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean = false,
    ): Result<CoinPage> = repository.getCoins(
        query = query,
        sort = sort,
        page = page,
        pageSize = pageSize,
        forceRefresh = forceRefresh,
    )
}

class GetCoinDetailsUseCase(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(
        id: String,
        forceRefresh: Boolean = false,
    ): Result<CoinDetails> = repository.getCoinDetails(id, forceRefresh)
}

class GetChartUseCase(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(
        id: String,
        range: ChartRange,
    ): Result<List<Double>> = repository.getChart(id, range)
}

class ToggleFavoriteUseCase(
    private val repository: CoinRepository,
) {
    operator fun invoke(coin: Coin) = repository.toggleFavorite(coin)
}

class ObserveFavoritesUseCase(
    private val repository: CoinRepository,
) {
    operator fun invoke(): StateFlow<List<Coin>> = repository.favorites
}
