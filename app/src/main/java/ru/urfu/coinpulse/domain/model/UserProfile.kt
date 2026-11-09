package ru.urfu.coinpulse.domain.model

data class UserProfile(
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String = "",
) {
    val isEmpty: Boolean
        get() = fullName.isBlank() &&
            position.isBlank() &&
            resumeUrl.isBlank() &&
            avatarUri.isBlank()
}

data class DownloadedDocument(
    val filePath: String,
    val mimeType: String,
    val displayName: String,
)

data class CameraTarget(
    val uri: String,
    val filePath: String,
)
