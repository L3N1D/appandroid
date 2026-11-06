package ru.urfu.coinpulse.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.CoinPage
import ru.urfu.coinpulse.domain.model.CoinSort
import ru.urfu.coinpulse.domain.repository.CoinRepository
import ru.urfu.coinpulse.domain.repository.FilterRepository

class GetCoinsUseCase(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(
        query: String,
        sort: CoinSort,
        filters: CoinFilters,
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean = false,
    ): Result<CoinPage> = repository.getCoins(
        query = query,
        sort = sort,
        filters = filters,
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
    suspend operator fun invoke(coin: Coin) = repository.toggleFavorite(coin)
}

class ObserveFavoritesUseCase(
    private val repository: CoinRepository,
) {
    operator fun invoke(): Flow<List<Coin>> = repository.favorites
}

class ObserveFiltersUseCase(
    private val repository: FilterRepository,
) {
    operator fun invoke(): Flow<CoinFilters> = repository.filters
}

class SaveFiltersUseCase(
    private val repository: FilterRepository,
) {
    suspend operator fun invoke(filters: CoinFilters) = repository.save(filters)
}

class ResetFiltersUseCase(
    private val repository: FilterRepository,
) {
    suspend operator fun invoke() = repository.reset()
}
