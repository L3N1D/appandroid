package ru.urfu.coinpulse.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.urfu.coinpulse.domain.error.AppException
import ru.urfu.coinpulse.domain.model.CameraTarget
import ru.urfu.coinpulse.domain.repository.AvatarRepository

/**
 * Copies picked images into app-internal storage and returns a FileProvider URI.
 * This avoids keeping fragile content-URI permissions and makes the avatar available
 * after a process restart / app update.
 */
class AvatarRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AvatarRepository {

    private val authority: String = "${context.packageName}.fileprovider"

    override fun createCameraTarget(): CameraTarget {
        val directory = File(context.filesDir, "avatars").apply { mkdirs() }
        val file = File(directory, "avatar_camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, authority, file)
        return CameraTarget(uri = uri.toString(), filePath = file.absolutePath)
    }

    override suspend fun importFromGallery(uri: String): Result<String> = withContext(ioDispatcher) {
        try {
            val source = Uri.parse(uri)
            val mimeType = context.contentResolver.getType(source)
            val directory = File(context.filesDir, "avatars").apply { mkdirs() }
            val file = File(
                directory,
                "avatar_gallery_${System.currentTimeMillis()}.${extensionForMime(mimeType)}",
            )
            val input = context.contentResolver.openInputStream(source)
                ?: return@withContext Result.failure(
                    AppException.Unknown(IOException("Cannot open selected image")),
                )
            input.use { sourceStream ->
                file.outputStream().use { output -> sourceStream.copyTo(output) }
            }
            val contentUri = FileProvider.getUriForFile(context, authority, file)
            Result.success(contentUri.toString())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(
                if (throwable is IOException) {
                    AppException.Unknown(throwable)
                } else {
                    AppException.Unknown(throwable)
                },
            )
        }
    }

    override fun deleteFile(filePath: String) {
        if (filePath.isBlank()) return
        runCatching { File(filePath).delete() }
    }

    private fun extensionForMime(mimeType: String?): String = when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/heic", "image/heif" -> "heic"
        else -> "jpg"
    }
}
