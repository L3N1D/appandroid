package ru.urfu.coinpulse.di

import android.content.Context
import androidx.room.Room
import ru.urfu.coinpulse.data.local.AvatarRepositoryImpl
import ru.urfu.coinpulse.data.local.CoinPulseDatabase
import ru.urfu.coinpulse.data.local.FilterRepositoryImpl
import ru.urfu.coinpulse.data.local.ProfileRepositoryImpl
import ru.urfu.coinpulse.data.network.NetworkModule
import ru.urfu.coinpulse.data.notifications.AlarmReminderScheduler
import ru.urfu.coinpulse.data.repository.CoinRepositoryImpl
import ru.urfu.coinpulse.domain.repository.AvatarRepository
import ru.urfu.coinpulse.domain.repository.CoinRepository
import ru.urfu.coinpulse.domain.repository.FilterRepository
import ru.urfu.coinpulse.domain.repository.ProfileRepository
import ru.urfu.coinpulse.domain.repository.ReminderScheduler
import ru.urfu.coinpulse.domain.usecase.CreateCameraTargetUseCase
import ru.urfu.coinpulse.domain.usecase.DeleteAvatarFileUseCase
import ru.urfu.coinpulse.domain.usecase.DownloadResumeUseCase
import ru.urfu.coinpulse.domain.usecase.GetChartUseCase
import ru.urfu.coinpulse.domain.usecase.GetCoinDetailsUseCase
import ru.urfu.coinpulse.domain.usecase.GetCoinsUseCase
import ru.urfu.coinpulse.domain.usecase.ImportGalleryAvatarUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveFiltersUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveProfileUseCase
import ru.urfu.coinpulse.domain.usecase.ResetFiltersUseCase
import ru.urfu.coinpulse.domain.usecase.SaveFiltersUseCase
import ru.urfu.coinpulse.domain.usecase.SaveProfileUseCase
import ru.urfu.coinpulse.domain.usecase.ScheduleClassReminderUseCase
import ru.urfu.coinpulse.domain.usecase.ToggleFavoriteUseCase
import ru.urfu.coinpulse.domain.usecase.ValidateProfileUseCase
import ru.urfu.coinpulse.presentation.filters.FilterBadgeCache

/**
 * Small manual dependency graph, shared by all ViewModels.
 *
 * [FilterBadgeCache] is intentionally a single in-memory instance: the Markets and
 * Filters screens both read/update it, but badge visibility is not persisted as a
 * separate setting. It is derived from the DataStore-backed filters.
 */
class AppContainer(context: Context) {

    private val networkModule = NetworkModule(context)

    private val database: CoinPulseDatabase = Room.databaseBuilder(
        context,
        CoinPulseDatabase::class.java,
        "coinpulse.db",
    ).fallbackToDestructiveMigration().build()

    private val repository: CoinRepository = CoinRepositoryImpl(
        api = networkModule.api,
        favoriteDao = database.favoriteDao(),
    )

    private val filterRepository: FilterRepository = FilterRepositoryImpl(context)

    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(
        context = context,
        httpClient = networkModule.client,
    )

    private val avatarRepository: AvatarRepository = AvatarRepositoryImpl(context)

    private val reminderScheduler: ReminderScheduler = AlarmReminderScheduler(context)

    val filterBadgeCache = FilterBadgeCache()

    val getCoins: GetCoinsUseCase = GetCoinsUseCase(repository)
    val getCoinDetails: GetCoinDetailsUseCase = GetCoinDetailsUseCase(repository)
    val getChart: GetChartUseCase = GetChartUseCase(repository)
    val toggleFavorite: ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    val observeFavorites: ObserveFavoritesUseCase = ObserveFavoritesUseCase(repository)

    val observeFilters: ObserveFiltersUseCase = ObserveFiltersUseCase(filterRepository)
    val saveFilters: SaveFiltersUseCase = SaveFiltersUseCase(filterRepository)
    val resetFilters: ResetFiltersUseCase = ResetFiltersUseCase(filterRepository)

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
