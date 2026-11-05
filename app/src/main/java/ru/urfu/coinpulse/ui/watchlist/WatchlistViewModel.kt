package ru.urfu.coinpulse.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.data.Coin
import ru.urfu.coinpulse.data.CoinRepository

data class WatchlistUiState(
    val coins: List<Coin> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val totalMarketCap: Long = 0,
    val isLoading: Boolean = true,
)

class WatchlistViewModel(
    private val repository: CoinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.favoriteIds.collect { favorites ->
                val coins = repository.getFavoriteCoins()
                _uiState.update {
                    WatchlistUiState(
                        coins = coins,
                        favoriteIds = favorites,
                        totalMarketCap = coins.sumOf { coin -> coin.marketCap },
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onRemoveFavorite(coinId: String) {
        repository.toggleFavorite(coinId)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                WatchlistViewModel(application.repository)
            }
        }
    }
}
