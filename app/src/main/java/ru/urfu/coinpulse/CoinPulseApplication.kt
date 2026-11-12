package ru.urfu.coinpulse

import android.app.Application
import ru.urfu.coinpulse.di.AppContainer
import ru.urfu.coinpulse.feature.profile.ProfileComponent
import ru.urfu.coinpulse.feature.profile.ProfileDependenciesProvider
import ru.urfu.coinpulse.feature.profile.ProfileFeature

class CoinPulseApplication : Application(), ProfileDependenciesProvider {

    lateinit var container: AppContainer
        private set

    override val profileComponent: ProfileComponent by lazy {
        ProfileFeature.create(this)
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
