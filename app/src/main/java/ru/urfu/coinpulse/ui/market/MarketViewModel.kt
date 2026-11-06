package ru.urfu.coinpulse.ui.market

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.data.Coin
import ru.urfu.coinpulse.data.CoinRepository
import ru.urfu.coinpulse.data.CoinSort
import ru.urfu.coinpulse.data.DEFAULT_PAGE_SIZE

data class MarketUiState(
    val query: String = "",
    val sort: CoinSort = CoinSort.MARKET_CAP,
    val coins: List<Coin> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 0,
    val total: Int = 0,
    val error: String? = null,
)

/**
 * MVVM: the screen only renders [MarketUiState] and forwards user intents to this
 * ViewModel. Paging, search debounce and sorting live here, not in composables.
 */
class MarketViewModel(
    private val repository: CoinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private var reloadJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.favoriteIds.collect { favorites ->
                _uiState.update { it.copy(favoriteIds = favorites) }
            }
        }
        reload(showFullScreenLoader = true)
    }

    fun onQueryChange(query: String) {
        if (query == _uiState.value.query) return
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            reload(showFullScreenLoader = false)
        }
    }

    fun onSortChange(sort: CoinSort) {
        if (sort == _uiState.value.sort) return
        _uiState.update { it.copy(sort = sort) }
        reload(showFullScreenLoader = false)
    }

    fun onRefresh() {
        reload(showFullScreenLoader = false)
    }

    fun onRetry() {
        reload(showFullScreenLoader = _uiState.value.coins.isEmpty())
    }

    private fun reload(showFullScreenLoader: Boolean) {
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
                val page = repository.getCoins(
                    query = _uiState.value.query,
                    sort = _uiState.value.sort,
                    page = 0,
                    pageSize = DEFAULT_PAGE_SIZE,
                )
                _uiState.update {
                    it.copy(
                        coins = page.items,
                        page = 0,
                        hasMore = page.hasMore,
                        total = page.total,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = throwable.message ?: "Could not load the market",
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing || !state.hasMore) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                val nextPage = repository.getCoins(
                    query = state.query,
                    sort = state.sort,
                    page = state.page + 1,
                    pageSize = DEFAULT_PAGE_SIZE,
                )
                _uiState.update { current ->
                    current.copy(
                        coins = (current.coins + nextPage.items).distinctBy { it.id },
                        page = current.page + 1,
                        hasMore = nextPage.hasMore,
                        total = nextPage.total,
                        isLoadingMore = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(isLoadingMore = false, hasMore = false) }
            }
        }
    }

    fun onFavoriteClick(coinId: String) {
        repository.toggleFavorite(coinId)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                MarketViewModel(application.repository)
            }
        }
    }
}
