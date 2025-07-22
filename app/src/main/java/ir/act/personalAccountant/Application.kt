package ir.act.personalAccountant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ir.act.personalAccountant.data.notification.NotificationService
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var notificationService: NotificationService

    override fun onCreate() {
        super.onCreate()
        // NotificationService will automatically start listening to app lifecycle
        // and handle notification management
    }
}