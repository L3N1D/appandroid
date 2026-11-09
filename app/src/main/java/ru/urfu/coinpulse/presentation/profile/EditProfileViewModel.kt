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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.coinpulse.CoinPulseApplication
import ru.urfu.coinpulse.domain.model.CameraTarget
import ru.urfu.coinpulse.domain.model.UserProfile
import ru.urfu.coinpulse.domain.usecase.CreateCameraTargetUseCase
import ru.urfu.coinpulse.domain.usecase.DeleteAvatarFileUseCase
import ru.urfu.coinpulse.domain.usecase.ImportGalleryAvatarUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveProfileUseCase
import ru.urfu.coinpulse.domain.usecase.SaveProfileUseCase
import ru.urfu.coinpulse.domain.usecase.ValidateProfileUseCase

data class EditProfileUiState(
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isImportingAvatar: Boolean = false,
    val pendingCamera: CameraTarget? = null,
    val error: String? = null,
)

sealed interface EditProfileEvent {
    data class LaunchCamera(val uri: String) : EditProfileEvent
    data object Saved : EditProfileEvent
}

class EditProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val validateProfile: ValidateProfileUseCase,
    private val createCameraTarget: CreateCameraTargetUseCase,
    private val importGalleryAvatar: ImportGalleryAvatarUseCase,
    private val deleteAvatarFile: DeleteAvatarFileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditProfileEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<EditProfileEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val profile = observeProfile().first()
            _uiState.update {
                it.copy(
                    fullName = profile.fullName,
                    position = profile.position,
                    resumeUrl = profile.resumeUrl,
                    avatarUri = profile.avatarUri,
                    isLoading = false,
                )
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, error = null) }
    }

    fun onPositionChange(value: String) {
        _uiState.update { it.copy(position = value, error = null) }
    }

    fun onResumeUrlChange(value: String) {
        _uiState.update { it.copy(resumeUrl = value, error = null) }
    }

    fun onGallerySelected(uri: String) {
        if (_uiState.value.isImportingAvatar) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingAvatar = true, error = null) }
            importGalleryAvatar(uri).fold(
                onSuccess = { localUri ->
                    _uiState.update {
                        it.copy(avatarUri = localUri, isImportingAvatar = false)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isImportingAvatar = false,
                            error = throwable.message ?: "Could not import the selected photo",
                        )
                    }
                },
            )
        }
    }

    fun onCameraSelected() {
        if (_uiState.value.isSaving) return
        val target = createCameraTarget()
        _uiState.update { it.copy(pendingCamera = target, error = null) }
        viewModelScope.launch { _events.emit(EditProfileEvent.LaunchCamera(target.uri)) }
    }

    fun onCameraResult(success: Boolean) {
        val target = _uiState.value.pendingCamera ?: return
        if (success) {
            _uiState.update {
                it.copy(
                    avatarUri = target.uri,
                    pendingCamera = null,
                    error = null,
                )
            }
        } else {
            deleteAvatarFile(target.filePath)
            _uiState.update {
                it.copy(
                    pendingCamera = null,
                    error = "Camera capture was cancelled.",
                )
            }
        }
    }

    fun onDiscardPendingCamera() {
        _uiState.value.pendingCamera?.let { deleteAvatarFile(it.filePath) }
        _uiState.update { it.copy(pendingCamera = null) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        val validationError = validateProfile(state.fullName, state.resumeUrl)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                saveProfile(
                    UserProfile(
                        fullName = state.fullName.trim(),
                        position = state.position.trim(),
                        resumeUrl = state.resumeUrl.trim(),
                        avatarUri = state.avatarUri,
                    ),
                )
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(EditProfileEvent.Saved)
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "Could not save the profile",
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
                EditProfileViewModel(
                    observeProfile = application.container.observeProfile,
                    saveProfile = application.container.saveProfile,
                    validateProfile = application.container.validateProfile,
                    createCameraTarget = application.container.createCameraTarget,
                    importGalleryAvatar = application.container.importGalleryAvatar,
                    deleteAvatarFile = application.container.deleteAvatarFile,
                )
            }
        }
    }
}
