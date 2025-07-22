package ir.act.personalAccountant.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val DAILY_REMINDER_WORK_NAME = "daily_reminder_work"
        private const val REMINDER_HOUR = 20 // 8 PM
        private const val REMINDER_MINUTE = 0
    }

    fun scheduleDailyReminder() {
        val workManager = WorkManager.getInstance(context)

        // Calculate delay until next 8 PM
        val now = Calendar.getInstance()
        val nextReminder = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If it's already past 8 PM today, schedule for tomorrow
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay =
            (nextReminder.timeInMillis - now.timeInMillis) / (1000 * 60) // Convert to minutes

        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyReminderRequest
        )
    }

    fun cancelDailyReminder() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }
}