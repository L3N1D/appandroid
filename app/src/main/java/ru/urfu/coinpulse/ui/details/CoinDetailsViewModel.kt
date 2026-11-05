package ru.urfu.coinpulse.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.data.ChartRange
import ru.urfu.coinpulse.data.CoinDetails
import ru.urfu.coinpulse.data.CoinRepository

data class CoinDetailsUiState(
    val coinId: String = "",
    val details: CoinDetails? = null,
    val chartRange: ChartRange = ChartRange.WEEK,
    val chartPoints: List<Double> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class CoinDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CoinRepository,
) : ViewModel() {

    private val coinId: String = checkNotNull(savedStateHandle["coinId"]) {
        "coinId is required to open the details screen"
    }

    private val _uiState = MutableStateFlow(CoinDetailsUiState(coinId = coinId))
    val uiState: StateFlow<CoinDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.favoriteIds.collect { favorites ->
                _uiState.update { it.copy(isFavorite = coinId in favorites) }
            }
        }
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val details = repository.getCoinDetails(coinId)
                if (details == null) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "This coin is not available")
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        details = details,
                        chartPoints = repository.getChartPoints(details.coin, it.chartRange),
                        isLoading = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Could not load the coin",
                    )
                }
            }
        }
    }

    fun onRangeSelected(range: ChartRange) {
        val details = _uiState.value.details ?: return
        if (range == _uiState.value.chartRange) return
        _uiState.update {
            it.copy(
                chartRange = range,
                chartPoints = repository.getChartPoints(details.coin, range),
            )
        }
    }

    fun onFavoriteClick() {
        repository.toggleFavorite(coinId)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                CoinDetailsViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = application.repository,
                )
            }
        }
    }
}
