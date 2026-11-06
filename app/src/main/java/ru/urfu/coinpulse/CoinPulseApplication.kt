package ru.urfu.coinpulse

import android.app.Application
import ru.urfu.coinpulse.di.AppContainer

class CoinPulseApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
