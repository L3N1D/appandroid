package ru.urfu.coinpulse.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.urfu.coinpulse.domain.error.AppException
import ru.urfu.coinpulse.domain.model.DownloadedDocument
import ru.urfu.coinpulse.domain.model.UserProfile
import ru.urfu.coinpulse.domain.repository.ProfileRepository

private val Context.profileDataStore by preferencesDataStore(name = "user_profile")

/**
 * The profile is a single record (name, position, resume link, avatar URI), so
 * Preferences DataStore is a better fit than a relational table. The avatar and
 * downloaded resume live in app-internal storage and are exposed through FileProvider.
 */
class ProfileRepositoryImpl(
    private val context: Context,
    private val httpClient: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProfileRepository {

    override val profile: Flow<UserProfile> = context.profileDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            UserProfile(
                fullName = preferences[Keys.FULL_NAME].orEmpty(),
                position = preferences[Keys.POSITION].orEmpty(),
                resumeUrl = preferences[Keys.RESUME_URL].orEmpty(),
                avatarUri = preferences[Keys.AVATAR_URI].orEmpty(),
                classTime = preferences[Keys.CLASS_TIME].orEmpty(),
            )
        }

    override suspend fun save(profile: UserProfile) {
        context.profileDataStore.edit { preferences ->
            preferences[Keys.FULL_NAME] = profile.fullName.trim()
            preferences[Keys.POSITION] = profile.position.trim()
            preferences[Keys.RESUME_URL] = profile.resumeUrl.trim()
            preferences[Keys.AVATAR_URI] = profile.avatarUri
            preferences[Keys.CLASS_TIME] = profile.classTime
        }
    }

    override suspend fun downloadResume(url: String): Result<DownloadedDocument> =
        withContext(ioDispatcher) {
            val httpUrl = url.trim().toHttpUrlOrNull()
            if (httpUrl == null || (httpUrl.scheme != "http" && httpUrl.scheme != "https")) {
                return@withContext Result.failure(AppException.InvalidUrl())
            }

            try {
                val request = Request.Builder()
                    .url(httpUrl)
                    .header("User-Agent", "CoinPulse/1.0 (Android practice)")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(response.toAppException())
                    }
                    val body = response.body
                        ?: return@withContext Result.failure(AppException.EmptyResponse())
                    val mimeType = body.contentType()
                        ?.let { "${it.type}/${it.subtype}" }
                        ?.takeIf { it.isNotBlank() }
                        ?: guessMimeType(httpUrl.encodedPath)
                    val displayName = buildDisplayName(httpUrl.encodedPath, mimeType)
                    val directory = File(context.cacheDir, "resume").apply { mkdirs() }
                    val file = File(directory, displayName)

                    body.byteStream().use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    Result.success(
                        DownloadedDocument(
                            filePath = file.absolutePath,
                            mimeType = mimeType,
                            displayName = displayName,
                        ),
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                val error = if (throwable is IOException) {
                    AppException.NoConnection(throwable)
                } else {
                    AppException.Unknown(throwable)
                }
                Result.failure(error)
            }
        }

    private fun okhttp3.Response.toAppException(): AppException = when (code) {
        429 -> AppException.RateLimited()
        in 500..599 -> AppException.Server(code)
        else -> AppException.Server(code)
    }

    private fun buildDisplayName(path: String, mimeType: String): String {
        val rawName = path.substringAfterLast('/').substringBefore('?').takeIf { it.isNotBlank() }
            ?: "resume"
        val safeName = rawName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val withExtension = if (safeName.contains('.')) {
            safeName
        } else {
            "$safeName.${extensionForMime(mimeType)}"
        }
        return "${System.currentTimeMillis()}_$withExtension"
    }

    private fun extensionForMime(mimeType: String): String = when (mimeType) {
        "application/pdf" -> "pdf"
        "text/html" -> "html"
        "text/plain" -> "txt"
        "image/png" -> "png"
        "image/jpeg" -> "jpg"
        else -> "bin"
    }

    private fun guessMimeType(path: String): String = when {
        path.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
        path.endsWith(".html", ignoreCase = true) -> "text/html"
        path.endsWith(".txt", ignoreCase = true) -> "text/plain"
        path.endsWith(".png", ignoreCase = true) -> "image/png"
        path.endsWith(".jpg", ignoreCase = true) ||
            path.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        else -> "application/octet-stream"
    }

    private object Keys {
        val FULL_NAME = stringPreferencesKey("full_name")
        val POSITION = stringPreferencesKey("position")
        val RESUME_URL = stringPreferencesKey("resume_url")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val CLASS_TIME = stringPreferencesKey("class_time")
    }
}
