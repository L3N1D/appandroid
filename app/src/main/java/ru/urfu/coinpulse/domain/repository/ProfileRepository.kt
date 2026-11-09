package ru.urfu.coinpulse.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.coinpulse.domain.model.DownloadedDocument
import ru.urfu.coinpulse.domain.model.UserProfile

interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun save(profile: UserProfile)
    suspend fun downloadResume(url: String): Result<DownloadedDocument>
}
