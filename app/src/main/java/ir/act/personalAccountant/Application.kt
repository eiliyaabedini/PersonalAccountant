package ir.act.personalAccountant

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.data.notification.NotificationService
import ir.act.personalAccountant.data.worker.DailyReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var dailyReminderScheduler: DailyReminderScheduler

    @Inject
    lateinit var notificationPreferences: NotificationPreferences

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager with Hilt factory
        WorkManager.initialize(this, workManagerConfiguration)
        
        // NotificationService will automatically start listening to app lifecycle
        // and handle notification management

        // Initialize daily reminder if enabled
        if (notificationPreferences.isDailyReminderEnabled) {
            dailyReminderScheduler.scheduleDailyReminder()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}