package ru.urfu.coinpulse.feature.profile.data.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import ru.urfu.coinpulse.feature.profile.domain.error.AppException
import ru.urfu.coinpulse.feature.profile.domain.model.UserProfile
import ru.urfu.coinpulse.feature.profile.domain.repository.ReminderScheduler

class AlarmReminderScheduler(
    private val context: Context,
) : ReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(profile: UserProfile): Result<Long> {
        val parts = profile.classTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()
        val minute = parts.getOrNull(1)?.toIntOrNull()
        if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
            return Result.failure(AppException.InvalidTime())
        }

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.timeInMillis <= System.currentTimeMillis()) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
            putExtra(ClassReminderReceiver.EXTRA_NAME, profile.fullName)
            putExtra(ClassReminderReceiver.EXTRA_TIME, profile.classTime)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.cancel(pendingIntent)
        try {
            val canExact = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.timeInMillis,
                    pendingIntent,
                )
            } else {
                // No exact-alarm special access: use a short inexact window instead.
                alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    trigger.timeInMillis,
                    INEXACT_WINDOW_MS,
                    pendingIntent,
                )
            }
        } catch (security: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger.timeInMillis,
                pendingIntent,
            )
        }
        return Result.success(trigger.timeInMillis)
    }

    override fun cancel() {
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    private companion object {
        const val REQUEST_CODE = 701
        const val INEXACT_WINDOW_MS = 15_000L
    }
}
