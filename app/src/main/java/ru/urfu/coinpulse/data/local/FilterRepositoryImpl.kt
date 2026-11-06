package ru.urfu.coinpulse.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.PerformanceFilter
import ru.urfu.coinpulse.domain.repository.FilterRepository

private val Context.filtersDataStore by preferencesDataStore(name = "coin_filters")

class FilterRepositoryImpl(
    private val context: Context,
) : FilterRepository {

    override val filters: Flow<CoinFilters> = context.filtersDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            CoinFilters(
                minMarketCap = preferences[Keys.MIN_MARKET_CAP] ?: 0L,
                performance = preferences[Keys.PERFORMANCE]
                    ?.let { name -> PerformanceFilter.entries.firstOrNull { it.name == name } }
                    ?: PerformanceFilter.ALL,
                minVolume24h = preferences[Keys.MIN_VOLUME_24H] ?: 0L,
            )
        }

    override suspend fun save(filters: CoinFilters) {
        context.filtersDataStore.edit { preferences ->
            preferences[Keys.MIN_MARKET_CAP] = filters.minMarketCap
            preferences[Keys.PERFORMANCE] = filters.performance.name
            preferences[Keys.MIN_VOLUME_24H] = filters.minVolume24h
        }
    }

    override suspend fun reset() {
        context.filtersDataStore.edit { preferences -> preferences.clear() }
    }

    private object Keys {
        val MIN_MARKET_CAP = longPreferencesKey("min_market_cap")
        val PERFORMANCE = stringPreferencesKey("performance")
        val MIN_VOLUME_24H = longPreferencesKey("min_volume_24h")
    }
}
