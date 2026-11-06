package ru.urfu.coinpulse.presentation.filters

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared, in-memory cache for the filter badge. The same instance is injected into
 * the Markets and Filters ViewModels through [ru.urfu.coinpulse.di.AppContainer].
 *
 * It is not persisted on its own: on the next launch it is populated again from the
 * DataStore-backed filters.
 */
class FilterBadgeCache {

    private val _visible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    fun update(active: Boolean) {
        _visible.value = active
    }
}
