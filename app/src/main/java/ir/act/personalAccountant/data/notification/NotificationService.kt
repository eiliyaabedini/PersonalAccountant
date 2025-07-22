package ir.act.personalAccountant.data.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.domain.usecase.NotificationUseCase
import ir.act.personalAccountant.util.NotificationPermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationUseCase: NotificationUseCase,
    private val notificationManager: BudgetNotificationManager,
    private val notificationPreferences: NotificationPreferences
) {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var notificationJob: Job? = null

    init {
        // Start notifications automatically if enabled
        refreshNotificationStatus()
    }

    private fun shouldShowNotifications(): Boolean {
        return notificationPreferences.isNotificationEnabled &&
                NotificationPermissionHelper.hasNotificationPermission(context)
    }

    fun startNotificationUpdates() {
        // Stop any existing notification job
        stopNotificationUpdates()

        // Only start if permissions and preferences allow
        if (!shouldShowNotifications()) {
            return
        }

        // Start listening to notification data changes
        notificationJob = notificationUseCase.getNotificationData()
            .onEach { data ->
                notificationManager.showBudgetNotification(data)
            }
            .launchIn(serviceScope)
    }

    fun stopNotificationUpdates() {
        notificationJob?.cancel()
        notificationJob = null
        notificationManager.clearNotification()
    }

    fun refreshNotificationStatus() {
        if (shouldShowNotifications()) {
            startNotificationUpdates()
        } else {
            stopNotificationUpdates()
        }
    }
}