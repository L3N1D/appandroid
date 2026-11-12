package ru.urfu.coinpulse.domain.repository

import ru.urfu.coinpulse.domain.model.UserProfile

/**
 * Platform reminder scheduling contract. Implemented with AlarmManager in the data layer.
 */
interface ReminderScheduler {
    fun schedule(profile: UserProfile): Result<Long>
    fun cancel()
}
