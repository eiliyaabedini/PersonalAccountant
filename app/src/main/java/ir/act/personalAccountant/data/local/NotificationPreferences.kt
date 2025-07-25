package ir.act.personalAccountant.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_CLOUD_SYNC_ENABLED = "cloud_sync_enabled"
    }

    var isNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, value).apply()

    var isDailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, value).apply()

    var isCloudSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, false) // Default disabled for privacy
        set(value) = prefs.edit().putBoolean(KEY_CLOUD_SYNC_ENABLED, value).apply()
}