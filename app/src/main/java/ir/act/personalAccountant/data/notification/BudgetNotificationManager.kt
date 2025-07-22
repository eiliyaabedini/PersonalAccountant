package ir.act.personalAccountant.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.MainActivity
import ir.act.personalAccountant.R
import ir.act.personalAccountant.domain.model.NotificationData
import ir.act.personalAccountant.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "budget_notification_channel"
        const val CHANNEL_NAME = "Budget Notifications"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows daily budget and expense information"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetNotification(data: NotificationData) {
        val notification = createNotification(data)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun clearNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createNotification(data: NotificationData): Notification {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(
            Constants.Navigation.NAVIGATE_TO_KEY,
            Constants.Navigation.NAVIGATE_TO_EXPENSE_ENTRY
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val iconResource = getBudgetStatusIcon(data)

        val (title, contentText, bigText) = getNotificationContent(data)

        val largeIcon = BitmapFactory.decodeResource(context.resources, iconResource)
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(iconResource)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    private fun getNotificationContent(data: NotificationData): Triple<String, String, String> {
        val title =
            "${Constants.NotificationContent.TODAY_EXPENSES_PREFIX} ${data.formattedTodayExpenses}"

        return if (!data.isBudgetConfigured) {
            val contentText =
                "${data.formattedTotalExpenses} ${Constants.NotificationContent.TOTAL_SUFFIX}"
            val bigText =
                "${Constants.NotificationContent.TOTAL_EXPENSES_PREFIX} ${data.formattedTotalExpenses}"
            Triple(title, contentText, bigText)
        } else {
            val contentText =
                "${data.formattedBudget} ${Constants.NotificationContent.BUDGET_SUFFIX}"
            val bigText =
                "${Constants.NotificationContent.TOTAL_EXPENSES_PREFIX} ${data.formattedTotalExpenses}\n${Constants.NotificationContent.DAILY_BUDGET_PREFIX} ${data.formattedBudget}\n${Constants.NotificationContent.REMAINING_PREFIX} ${data.formattedRemaining}"
            Triple(title, contentText, bigText)
        }
    }

    private fun getBudgetStatusIcon(data: NotificationData): Int {
        return if (!data.isBudgetConfigured) {
            R.mipmap.happy_owl
        } else if (data.dailyBudget != null && data.todayExpenses <= data.dailyBudget) {
            R.mipmap.happy_owl
        } else {
            R.mipmap.sad_owl
        }
    }
}