package ru.urfu.coinpulse.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.domain.model.Coin
import ru.urfu.coinpulse.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.coinpulse.domain.usecase.ToggleFavoriteUseCase

data class WatchlistUiState(
    val coins: List<Coin> = emptyList(),
    val totalMarketCap: Long = 0,
    val isLoading: Boolean = true,
)

class WatchlistViewModel(
    private val observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeFavorites().collect { favorites ->
                _uiState.value = WatchlistUiState(
                    coins = favorites,
                    totalMarketCap = favorites.sumOf { coin -> coin.marketCap },
                    isLoading = false,
                )
            }
        }
    }

    fun onRemoveFavorite(coinId: String) {
        val coin = _uiState.value.coins.firstOrNull { it.id == coinId } ?: return
        viewModelScope.launch { toggleFavorite(coin) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                WatchlistViewModel(
                    observeFavorites = application.container.observeFavorites,
                    toggleFavorite = application.container.toggleFavorite,
                )
            }
        }
    }
}
