package ru.urfu.coinpulse.presentation.profile

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
import ru.urfu.coinpulse.domain.model.DownloadedDocument
import ru.urfu.coinpulse.domain.model.UserProfile
import ru.urfu.coinpulse.domain.usecase.DownloadResumeUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveProfileUseCase

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isDownloadingResume: Boolean = false,
    val error: String? = null,
)

sealed interface ProfileEvent {
    data class OpenDocument(val document: DownloadedDocument) : ProfileEvent
}

class ProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val downloadResume: DownloadResumeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeProfile().collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    fun onResumeClick() {
        val state = _uiState.value
        if (state.isDownloadingResume) return
        val url = state.profile.resumeUrl.trim()
        if (url.isBlank()) {
            _uiState.update { it.copy(error = "Add a resume URL in Edit profile first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingResume = true, error = null) }
            downloadResume(url).fold(
                onSuccess = { document ->
                    _uiState.update { it.copy(isDownloadingResume = false) }
                    _events.emit(ProfileEvent.OpenDocument(document))
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isDownloadingResume = false,
                            error = throwable.message ?: "Could not download the resume",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CoinPulseApplication
                ProfileViewModel(
                    observeProfile = application.container.observeProfile,
                    downloadResume = application.container.downloadResume,
                )
            }
        }
    }
}
