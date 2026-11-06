package ru.urfu.coinpulse.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.CoinSort
import ru.urfu.coinpulse.domain.model.DEFAULT_PAGE_SIZE
import ru.urfu.coinpulse.domain.usecase.GetCoinsUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveFiltersUseCase
import ru.urfu.coinpulse.domain.usecase.ToggleFavoriteUseCase
import ru.urfu.coinpulse.presentation.filters.FilterBadgeCache

data class MarketUiState(
    val query: String = "",
    val sort: CoinSort = CoinSort.MARKET_CAP,
    val filters: CoinFilters = CoinFilters(),
    val coins: List<Coin> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 0,
    val total: Int = 0,
    val showFilterBadge: Boolean = false,
    val error: String? = null,
)

/**
 * Presentation layer: the screen renders [MarketUiState] and forwards intents here.
 * All network work goes through use cases on a background dispatcher.
 *
 * Persisted filters arrive as a Flow from DataStore. The shared [FilterBadgeCache]
 * is updated here and in FiltersViewModel; the cache itself is never persisted.
 */
class MarketViewModel(
    private val getCoins: GetCoinsUseCase,
    private val observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val observeFilters: ObserveFiltersUseCase,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private var reloadJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            observeFavorites().collect { favorites ->
                _uiState.update { it.copy(favoriteIds = favorites.map(Coin::id).toSet()) }
            }
        }
        viewModelScope.launch {
            observeFilters().distinctUntilChanged().collect { filters ->
                badgeCache.update(filters.isActive)
                _uiState.update {
                    it.copy(
                        filters = filters,
                        showFilterBadge = filters.isActive,
                    )
                }
                reload(
                    showFullScreenLoader = _uiState.value.coins.isEmpty(),
                    forceRefresh = false,
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        if (query == _uiState.value.query) return
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            reload(showFullScreenLoader = false, forceRefresh = false)
        }
    }

    fun onSortChange(sort: CoinSort) {
        if (sort == _uiState.value.sort) return
        _uiState.update { it.copy(sort = sort) }
        reload(showFullScreenLoader = false, forceRefresh = false)
    }

    fun onRefresh() {
        reload(showFullScreenLoader = false, forceRefresh = true)
    }

    fun onRetry() {
        reload(
            showFullScreenLoader = _uiState.value.coins.isEmpty(),
            forceRefresh = true,
        )
    }

    private fun reload(showFullScreenLoader: Boolean, forceRefresh: Boolean) {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = showFullScreenLoader && it.coins.isEmpty(),
                    isRefreshing = !showFullScreenLoader && it.coins.isNotEmpty(),
                    error = null,
                )
            }
            try {
                val result = getCoins(
                    query = _uiState.value.query,
                    sort = _uiState.value.sort,
                    filters = _uiState.value.filters,
                    page = 0,
                    pageSize = DEFAULT_PAGE_SIZE,
                    forceRefresh = forceRefresh,
                )
                result.fold(
                    onSuccess = { pageResult ->
                        _uiState.update {
                            it.copy(
                                coins = pageResult.items,
                                page = 0,
                                hasMore = pageResult.hasMore,
                                total = pageResult.total,
                                isLoading = false,
                                isRefreshing = false,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = throwable.message ?: "Could not load the market",
                            )
                        }
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing || !state.hasMore) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                val result = getCoins(
                    query = state.query,
                    sort = state.sort,
                    filters = state.filters,
                    page = state.page + 1,
                    pageSize = DEFAULT_PAGE_SIZE,
                    forceRefresh = false,
                )
                result.fold(
                    onSuccess = { pageResult ->
                        _uiState.update { current ->
                            current.copy(
                                coins = (current.coins + pageResult.items).distinctBy { it.id },
                                page = current.page + 1,
                                hasMore = pageResult.hasMore,
                                total = pageResult.total,
                                isLoadingMore = false,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = throwable.message ?: "Could not load more coins",
                            )
                        }
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            }
        }
    }

    fun onFavoriteClick(coinId: String) {
        val coin = _uiState.value.coins.firstOrNull { it.id == coinId } ?: return
        viewModelScope.launch { toggleFavorite(coin) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                MarketViewModel(
                    getCoins = application.container.getCoins,
                    observeFavorites = application.container.observeFavorites,
                    toggleFavorite = application.container.toggleFavorite,
                    observeFilters = application.container.observeFilters,
                    badgeCache = application.container.filterBadgeCache,
                )
            }
        }
    }
}
