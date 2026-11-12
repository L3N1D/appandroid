package ru.urfu.coinpulse.feature.profile.domain.repository

import ru.urfu.coinpulse.feature.profile.domain.model.CameraTarget

interface AvatarRepository {
    fun createCameraTarget(): CameraTarget
    suspend fun importFromGallery(uri: String): Result<String>
    fun deleteFile(filePath: String)
}
