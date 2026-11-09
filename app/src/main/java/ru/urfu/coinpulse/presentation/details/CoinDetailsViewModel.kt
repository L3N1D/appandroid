package ru.urfu.coinpulse.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.domain.model.ChartRange
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.model.CoinDetails
import ru.urfu.coinpulse.domain.usecase.GetChartUseCase
import ru.urfu.coinpulse.domain.usecase.GetCoinDetailsUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.coinpulse.domain.usecase.ToggleFavoriteUseCase

data class CoinDetailsUiState(
    val coinId: String = "",
    val details: CoinDetails? = null,
    val chartRange: ChartRange = ChartRange.WEEK,
    val chartPoints: List<Double> = emptyList(),
    val isChartLoading: Boolean = false,
    val chartError: String? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class CoinDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCoinDetails: GetCoinDetailsUseCase,
    private val getChart: GetChartUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val observeFavorites: ObserveFavoritesUseCase,
) : ViewModel() {

    private val coinId: String = checkNotNull(savedStateHandle["coinId"]) {
        "coinId is required to open the details screen"
    }

    private val _uiState = MutableStateFlow(CoinDetailsUiState(coinId = coinId))
    val uiState: StateFlow<CoinDetailsUiState> = _uiState.asStateFlow()

    private var chartJob: Job? = null

    init {
        viewModelScope.launch {
            observeFavorites().collect { favorites ->
                _uiState.update { it.copy(isFavorite = favorites.any { coin -> coin.id == coinId }) }
            }
        }
        loadDetails(forceRefresh = false)
    }

    fun loadDetails(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = getCoinDetails(id = coinId, forceRefresh = forceRefresh)
                result.fold(
                    onSuccess = { details ->
                        val range = _uiState.value.chartRange
                        val offlinePoints = sparklineFor(details, range)
                        _uiState.update {
                            it.copy(
                                details = details,
                                chartPoints = offlinePoints.orEmpty(),
                                isChartLoading = offlinePoints == null,
                                isLoading = false,
                            )
                        }
                        if (offlinePoints == null) loadChart(range)
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = throwable.message ?: "Could not load the coin",
                            )
                        }
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            }
        }
    }

    fun onRangeSelected(range: ChartRange) {
        val state = _uiState.value
        if (range == state.chartRange && (state.chartPoints.isNotEmpty() || state.isChartLoading)) return

        _uiState.update { it.copy(chartRange = range, chartError = null) }
        val details = state.details
        val offlinePoints = details?.let { sparklineFor(it, range) }
        if (offlinePoints != null) {
            _uiState.update {
                it.copy(chartPoints = offlinePoints, isChartLoading = false, chartError = null)
            }
            return
        }
        loadChart(range)
    }

    fun retryChart() {
        loadChart(_uiState.value.chartRange)
    }

    private fun loadChart(range: ChartRange) {
        chartJob?.cancel()
        chartJob = viewModelScope.launch {
            _uiState.update { it.copy(isChartLoading = true, chartError = null) }
            try {
                val result = getChart(coinId, range)
                result.fold(
                    onSuccess = { points ->
                        _uiState.update { current ->
                            if (current.chartRange == range) {
                                current.copy(
                                    chartPoints = points,
                                    isChartLoading = false,
                                    chartError = null,
                                )
                            } else {
                                current
                            }
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update { current ->
                            if (current.chartRange == range) {
                                current.copy(
                                    isChartLoading = false,
                                    chartError = throwable.message ?: "Could not load the chart",
                                )
                            } else {
                                current
                            }
                        }
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            }
        }
    }

    private fun sparklineFor(details: CoinDetails, range: ChartRange): List<Double>? {
        val sparkline = details.coin.sparkline
        return when {
            range == ChartRange.WEEK && sparkline.isNotEmpty() -> sparkline
            range == ChartRange.DAY && sparkline.size >= 24 -> sparkline.takeLast(24)
            else -> null
        }
    }

    fun onFavoriteClick() {
        val coin: Coin = _uiState.value.details?.coin ?: return
        viewModelScope.launch { toggleFavorite(coin) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                CoinDetailsViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    getCoinDetails = application.container.getCoinDetails,
                    getChart = application.container.getChart,
                    toggleFavorite = application.container.toggleFavorite,
                    observeFavorites = application.container.observeFavorites,
                )
            }
        }
    }
}
