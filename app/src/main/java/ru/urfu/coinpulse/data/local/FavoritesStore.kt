package ru.urfu.coinpulse.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.urfu.coinpulse.domain.model.Coin

/**
 * In-memory watchlist. Favorites keep a full [Coin] snapshot so the watchlist
 * works even when the network is unavailable (and after a markets refresh the
 * snapshots are updated with fresh prices).
 */
class FavoritesStore {

    private val _favorites = MutableStateFlow<List<Coin>>(emptyList())
    val favorites: StateFlow<List<Coin>> = _favorites.asStateFlow()

    fun toggle(coin: Coin) {
        _favorites.update { current ->
            if (current.any { it.id == coin.id }) {
                current.filterNot { it.id == coin.id }
            } else {
                (current + coin).sortedBy { it.rank }
            }
        }
    }

    fun isFavorite(id: String): Boolean = _favorites.value.any { it.id == id }

    fun updateSnapshots(coins: List<Coin>) {
        if (coins.isEmpty()) return
        val byId = coins.associateBy { it.id }
        _favorites.update { current ->
            if (current.none { it.id in byId }) return@update current
            current.map { favorite -> byId[favorite.id] ?: favorite }
        }
    }
}
