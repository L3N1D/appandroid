package ru.urfu.coinpulse.feature.profile.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.urfu.coinpulse.feature.profile.R

class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        createChannel(context)

        // Do not reference :app's MainActivity from this module. Resolve it at runtime.
        val openAppIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        val contentIntent = openAppIntent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val text = if (name.isBlank()) {
            "It's time for your favorite class!"
        } else {
            "It's time for your favorite class, $name!"
        }
        val title = if (time.isBlank()) "Class reminder" else "Class reminder · $time"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Class reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Reminders about your favorite class"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_CLASS_REMINDER = "ru.urfu.coinpulse.CLASS_REMINDER"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_TIME = "extra_time"
        const val CHANNEL_ID = "class_reminder"
        const val NOTIFICATION_ID = 701
    }
}
