package ru.urfu.coinpulse.domain.repository

import ru.urfu.coinpulse.domain.model.CameraTarget

interface AvatarRepository {
    fun createCameraTarget(): CameraTarget
    suspend fun importFromGallery(uri: String): Result<String>
    fun deleteFile(filePath: String)
}
