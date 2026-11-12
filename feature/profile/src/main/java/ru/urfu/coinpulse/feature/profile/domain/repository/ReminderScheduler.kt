package ru.urfu.coinpulse.feature.profile.domain.repository

import ru.urfu.coinpulse.feature.profile.domain.model.UserProfile

/**
 * Platform reminder scheduling contract. Implemented with AlarmManager in the data layer.
 */
interface ReminderScheduler {
    fun schedule(profile: UserProfile): Result<Long>
    fun cancel()
}
