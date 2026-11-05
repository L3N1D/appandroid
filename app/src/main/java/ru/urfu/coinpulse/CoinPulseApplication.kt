package ru.urfu.coinpulse

import android.app.Application
import ru.urfu.coinpulse.data.CoinRepository
import ru.urfu.coinpulse.data.MockCoinRepository

/**
 * Tiny manual service locator. Keeps the practice free of DI frameworks while still
 * giving every ViewModel the same repository instance (important for the shared
 * watchlist state).
 */
class CoinPulseApplication : Application() {

    val repository: CoinRepository by lazy { MockCoinRepository() }
}
