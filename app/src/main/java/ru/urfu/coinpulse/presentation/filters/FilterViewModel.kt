package ru.urfu.coinpulse.presentation.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.domain.model.CoinFilters
import ru.urfu.coinpulse.domain.model.PerformanceFilter
import ru.urfu.coinpulse.domain.usecase.ObserveFiltersUseCase
import ru.urfu.coinpulse.domain.usecase.SaveFiltersUseCase

data class FilterUiState(
    val filters: CoinFilters = CoinFilters(),
    val draft: CoinFilters = CoinFilters(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val badgeVisible: Boolean = false,
    val saveError: String? = null,
) {
    val hasChanges: Boolean get() = draft != filters
    val draftIsActive: Boolean get() = draft.isActive
}

enum class FilterEvent {
    APPLIED,
}

class FilterViewModel(
    private val observeFilters: ObserveFiltersUseCase,
    private val saveFilters: SaveFiltersUseCase,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FilterEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<FilterEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeFilters().collect { filters ->
                badgeCache.update(filters.isActive)
                _uiState.update {
                    it.copy(
                        filters = filters,
                        draft = filters,
                        isLoading = false,
                    )
                }
            }
        }
        viewModelScope.launch {
            badgeCache.visible.collect { visible ->
                _uiState.update { it.copy(badgeVisible = visible) }
            }
        }
    }

    fun setMinMarketCap(value: Long) {
        _uiState.update { it.copy(draft = it.draft.copy(minMarketCap = value)) }
    }

    fun setPerformance(value: PerformanceFilter) {
        _uiState.update { it.copy(draft = it.draft.copy(performance = value)) }
    }

    fun setMinVolume24h(value: Long) {
        _uiState.update { it.copy(draft = it.draft.copy(minVolume24h = value)) }
    }

    fun resetDraft() {
        _uiState.update { it.copy(draft = CoinFilters()) }
    }

    fun applyFilters() {
        if (_uiState.value.isSaving) return
        val filters = _uiState.value.draft
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                saveFilters(filters)
                badgeCache.update(filters.isActive)
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(FilterEvent.APPLIED)
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = throwable.message ?: "Could not save filters",
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                FilterViewModel(
                    observeFilters = application.container.observeFilters,
                    saveFilters = application.container.saveFilters,
                    badgeCache = application.container.filterBadgeCache,
                )
            }
        }
    }
}
