package ru.urfu.coinpulse.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.coinpulse.domain.model.CoinFilters

interface FilterRepository {
    val filters: Flow<CoinFilters>
    suspend fun save(filters: CoinFilters)
    suspend fun reset()
}
