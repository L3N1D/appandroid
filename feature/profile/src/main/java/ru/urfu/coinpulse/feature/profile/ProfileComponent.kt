package ru.urfu.coinpulse.feature.profile

import android.content.Context
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import ru.urfu.coinpulse.feature.profile.data.local.AvatarRepositoryImpl
import ru.urfu.coinpulse.feature.profile.data.local.ProfileRepositoryImpl
import ru.urfu.coinpulse.feature.profile.data.notifications.AlarmReminderScheduler
import ru.urfu.coinpulse.feature.profile.domain.repository.AvatarRepository
import ru.urfu.coinpulse.feature.profile.domain.repository.ProfileRepository
import ru.urfu.coinpulse.feature.profile.domain.repository.ReminderScheduler
import ru.urfu.coinpulse.feature.profile.domain.usecase.CreateCameraTargetUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.DeleteAvatarFileUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.DownloadResumeUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.ImportGalleryAvatarUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.ObserveProfileUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.SaveProfileUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.ScheduleClassReminderUseCase
import ru.urfu.coinpulse.feature.profile.domain.usecase.ValidateProfileUseCase

/**
 * Dependency graph owned by the Profile feature. The app only depends on this
 * module and implements [ProfileDependenciesProvider]; this module never imports
 * anything from `:app`.
 */
class ProfileComponent(context: Context) {

    private val appContext = context.applicationContext

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(
        context = appContext,
        httpClient = httpClient,
    )
    private val avatarRepository: AvatarRepository = AvatarRepositoryImpl(appContext)
    private val reminderScheduler: ReminderScheduler = AlarmReminderScheduler(appContext)

    val observeProfile: ObserveProfileUseCase = ObserveProfileUseCase(profileRepository)
    val saveProfile: SaveProfileUseCase = SaveProfileUseCase(profileRepository)
    val downloadResume: DownloadResumeUseCase = DownloadResumeUseCase(profileRepository)
    val createCameraTarget: CreateCameraTargetUseCase = CreateCameraTargetUseCase(avatarRepository)
    val importGalleryAvatar: ImportGalleryAvatarUseCase = ImportGalleryAvatarUseCase(avatarRepository)
    val deleteAvatarFile: DeleteAvatarFileUseCase = DeleteAvatarFileUseCase(avatarRepository)
    val validateProfile: ValidateProfileUseCase = ValidateProfileUseCase()
    val scheduleClassReminder: ScheduleClassReminderUseCase =
        ScheduleClassReminderUseCase(reminderScheduler)
}

interface ProfileDependenciesProvider {
    val profileComponent: ProfileComponent
}

object ProfileFeature {
    fun create(context: Context): ProfileComponent = ProfileComponent(context)
}
