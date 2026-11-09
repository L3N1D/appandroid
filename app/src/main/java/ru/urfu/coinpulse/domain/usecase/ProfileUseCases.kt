package ru.urfu.coinpulse.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.coinpulse.domain.model.CameraTarget
import ru.urfu.coinpulse.domain.model.DownloadedDocument
import ru.urfu.coinpulse.domain.model.UserProfile
import ru.urfu.coinpulse.domain.repository.AvatarRepository
import ru.urfu.coinpulse.domain.repository.ProfileRepository
import ru.urfu.coinpulse.domain.validation.ProfileValidator

class ObserveProfileUseCase(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): Flow<UserProfile> = repository.profile
}

class SaveProfileUseCase(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(profile: UserProfile) = repository.save(profile)
}

class DownloadResumeUseCase(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(url: String): Result<DownloadedDocument> =
        repository.downloadResume(url)
}

class CreateCameraTargetUseCase(
    private val repository: AvatarRepository,
) {
    operator fun invoke(): CameraTarget = repository.createCameraTarget()
}

class ImportGalleryAvatarUseCase(
    private val repository: AvatarRepository,
) {
    suspend operator fun invoke(uri: String): Result<String> = repository.importFromGallery(uri)
}

class DeleteAvatarFileUseCase(
    private val repository: AvatarRepository,
) {
    operator fun invoke(filePath: String) = repository.deleteFile(filePath)
}

class ValidateProfileUseCase {
    operator fun invoke(fullName: String, resumeUrl: String): String? =
        ProfileValidator.validate(fullName, resumeUrl)
}
