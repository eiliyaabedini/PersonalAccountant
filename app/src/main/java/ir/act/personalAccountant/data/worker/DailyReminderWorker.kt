package ir.act.personalAccountant.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.data.notification.BudgetNotificationManager
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val notificationManager: BudgetNotificationManager,
    private val notificationPreferences: NotificationPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Check if daily reminder is enabled
                if (!notificationPreferences.isDailyReminderEnabled) {
                    return@withContext Result.success()
                }

                // Check if user has added expenses today
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val todayEnd = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val todayExpenses = expenseRepository.getExpensesByDateRange(todayStart, todayEnd)

                // If no expenses for today, show reminder notification
                if (todayExpenses.isEmpty()) {
                    notificationManager.showReminderNotification()
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}