package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.NotificationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class NotificationUseCaseImpl @Inject constructor(
    private val budgetUseCase: BudgetUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase
) : NotificationUseCase {

    override fun getNotificationData(): Flow<NotificationData> {
        return combine(
            budgetUseCase.getBudgetData(),
            getCurrencySettingsUseCase()
        ) { budgetData, currencySettings ->
            val calendar = Calendar.getInstance()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val monthFormatter = SimpleDateFormat("MMMM", Locale.getDefault())
            val monthName = monthFormatter.format(calendar.time)
            val remainingAmount =
                budgetData.recommendedDailyExpenseBudget - budgetData.todayExpenses

            val isBudgetConfigured = budgetData.recommendedDailyExpenseBudget > 0

            NotificationData(
                dayOfMonth = dayOfMonth,
                monthName = monthName,
                todayExpenses = budgetData.todayExpenses,
                totalExpenses = budgetData.totalExpensesToDate,
                dailyBudget = if (isBudgetConfigured) budgetData.recommendedDailyExpenseBudget else null,
                formattedTodayExpenses = CurrencyFormatter.formatCurrency(
                    budgetData.todayExpenses,
                    currencySettings
                ),
                formattedTotalExpenses = CurrencyFormatter.formatCurrency(
                    budgetData.totalExpensesToDate,
                    currencySettings
                ),
                formattedBudget = if (isBudgetConfigured) CurrencyFormatter.formatCurrency(
                    budgetData.recommendedDailyExpenseBudget,
                    currencySettings
                ) else null,
                formattedRemaining = if (isBudgetConfigured) {
                    if (remainingAmount >= 0) {
                        "${
                            CurrencyFormatter.formatCurrency(
                                remainingAmount,
                                currencySettings
                            )
                        } available"
                    } else {
                        "${
                            CurrencyFormatter.formatCurrency(
                                remainingAmount,
                                currencySettings
                            )
                        } over budget"
                    }
                } else null,
                isBudgetConfigured = isBudgetConfigured
            )
        }
    }
}