package ru.urfu.coinpulse.di

import android.content.Context
import ru.urfu.coinpulse.data.local.FavoritesStore
import ru.urfu.coinpulse.data.network.NetworkModule
import ru.urfu.coinpulse.data.repository.CoinRepositoryImpl
import ru.urfu.coinpulse.domain.repository.CoinRepository
import ru.urfu.coinpulse.domain.usecase.GetChartUseCase
import ru.urfu.coinpulse.domain.usecase.GetCoinDetailsUseCase
import ru.urfu.coinpulse.domain.usecase.GetCoinsUseCase
import ru.urfu.coinpulse.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.coinpulse.domain.usecase.ToggleFavoriteUseCase

/**
 * Small manual dependency graph, shared by all ViewModels.
 */
class AppContainer(context: Context) {

    private val networkModule = NetworkModule(context)
    private val favoritesStore = FavoritesStore()

    private val repository: CoinRepository = CoinRepositoryImpl(
        api = networkModule.api,
        favoritesStore = favoritesStore,
    )

    val getCoins: GetCoinsUseCase = GetCoinsUseCase(repository)
    val getCoinDetails: GetCoinDetailsUseCase = GetCoinDetailsUseCase(repository)
    val getChart: GetChartUseCase = GetChartUseCase(repository)
    val toggleFavorite: ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    val observeFavorites: ObserveFavoritesUseCase = ObserveFavoritesUseCase(repository)
}
