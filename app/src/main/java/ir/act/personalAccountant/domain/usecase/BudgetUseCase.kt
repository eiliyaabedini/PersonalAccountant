package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.BudgetSettings
import ir.act.personalAccountant.domain.model.BudgetStatus
import ir.act.personalAccountant.domain.model.SavingGoalStatus
import ir.act.personalAccountant.domain.repository.BudgetRepository
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

interface BudgetUseCase {
    fun getBudgetSettings(): Flow<BudgetSettings>
    suspend fun updateBudgetSettings(budgetSettings: BudgetSettings)
    suspend fun updateNetSalary(netSalary: Double)
    suspend fun updateTotalRent(totalRent: Double)
    suspend fun setBudgetConfigured(isConfigured: Boolean)
    suspend fun updateSavingGoal(savingGoal: Double)
    fun getBudgetData(): Flow<BudgetData>
}

class BudgetUseCaseImpl @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : BudgetUseCase {

    override fun getBudgetSettings(): Flow<BudgetSettings> {
        return budgetRepository.getBudgetSettings()
    }

    override suspend fun updateBudgetSettings(budgetSettings: BudgetSettings) {
        budgetRepository.updateBudgetSettings(budgetSettings)
    }

    override suspend fun updateNetSalary(netSalary: Double) {
        budgetRepository.updateNetSalary(netSalary)
    }

    override suspend fun updateTotalRent(totalRent: Double) {
        budgetRepository.updateTotalRent(totalRent)
    }

    override suspend fun setBudgetConfigured(isConfigured: Boolean) {
        budgetRepository.setBudgetConfigured(isConfigured)
    }

    override suspend fun updateSavingGoal(savingGoal: Double) {
        budgetRepository.updateSavingGoal(savingGoal)
    }

    override fun getBudgetData(): Flow<BudgetData> {
        return combine(
            budgetRepository.getBudgetSettings(),
            expenseRepository.getAllExpenses()
        ) { budgetSettings, expenses ->
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)
            val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val dailyIncome = if (totalDaysInMonth > 0) budgetSettings.netSalary / totalDaysInMonth else 0.0
            val dailyRent = if (totalDaysInMonth > 0) budgetSettings.totalRent / totalDaysInMonth else 0.0
            
            val totalIncomeToDate = dailyIncome * currentDay
            val totalRentToDate = dailyRent * currentDay
            
            // Filter expenses for current month
            val currentMonthExpenses = expenses.filter { expense ->
                val expenseCalendar = Calendar.getInstance()
                expenseCalendar.timeInMillis = expense.timestamp
                expenseCalendar.get(Calendar.YEAR) == currentYear && 
                expenseCalendar.get(Calendar.MONTH) + 1 == currentMonth
            }
            
            val totalExpensesToDate = currentMonthExpenses.sumOf { it.amount }
            val totalExpensesWithRent = totalExpensesToDate + totalRentToDate
            
            // Calculate average daily expenses (excluding rent)
            val averageDailyExpenses = if (currentDay > 0) totalExpensesToDate / currentDay else 0.0
            
            // Calculate estimated end of month balance
            // (Total monthly income) - (projected expenses for full month) - (total monthly rent)
            val projectedMonthlyExpenses = averageDailyExpenses * totalDaysInMonth
            val totalMonthlyIncome = dailyIncome * totalDaysInMonth
            val totalMonthlyRent = dailyRent * totalDaysInMonth
            val estimatedEndOfMonthBalance = totalMonthlyIncome - projectedMonthlyExpenses - totalMonthlyRent
            
            val budgetStatus = when {
                totalExpensesWithRent > totalIncomeToDate -> BudgetStatus.RED
                totalExpensesWithRent > totalIncomeToDate * 0.8 -> BudgetStatus.MIDDLE
                else -> BudgetStatus.GOOD
            }

            // Saving goal calculations - automatically use 20% of salary or user-defined amount
            val autoSavingGoal = budgetSettings.netSalary * 0.20 // 20% based on 50/30/20 rule
            val savingGoal = if (budgetSettings.monthlySavingGoal > 0) {
                budgetSettings.monthlySavingGoal
            } else {
                autoSavingGoal
            }

            val currentBalance = totalIncomeToDate - totalExpensesToDate - totalRentToDate
            val daysRemaining = totalDaysInMonth - currentDay
            val dailyNetIncomeAfterRent = dailyIncome - dailyRent

            // Calculate today's expenses for color logic
            val todayExpenses = currentMonthExpenses.filter { expense ->
                val expenseCalendar = Calendar.getInstance()
                expenseCalendar.timeInMillis = expense.timestamp
                expenseCalendar.get(Calendar.DAY_OF_MONTH) == currentDay
            }.sumOf { it.amount }

            val (dailySavingsNeeded, recommendedDailyExpenseBudget, savingGoalStatus) = if (budgetSettings.netSalary > 0) {
                val amountNeededForGoal = savingGoal - currentBalance

                val dailySavingsNeeded = if (daysRemaining > 0) {
                    amountNeededForGoal / daysRemaining
                } else 0.0

                val recommendedDailyExpenseBudget = if (daysRemaining > 0) {
                    (dailyNetIncomeAfterRent - dailySavingsNeeded).coerceAtLeast(0.0)
                } else 0.0

                val status = when {
                    currentBalance >= savingGoal -> SavingGoalStatus.GOAL_EXCEEDED
                    amountNeededForGoal <= 0 -> SavingGoalStatus.GOAL_EXCEEDED
                    dailySavingsNeeded > dailyNetIncomeAfterRent -> SavingGoalStatus.NEED_ADJUSTMENT
                    else -> SavingGoalStatus.ON_TRACK
                }

                Triple(dailySavingsNeeded, recommendedDailyExpenseBudget, status)
            } else {
                Triple(0.0, 0.0, SavingGoalStatus.NOT_SET)
            }
            
            BudgetData(
                currentDay = currentDay,
                totalDaysInMonth = totalDaysInMonth,
                dailyIncome = dailyIncome,
                dailyRent = dailyRent,
                totalIncomeToDate = totalIncomeToDate,
                totalRentToDate = totalRentToDate,
                totalExpensesToDate = totalExpensesToDate,
                averageDailyExpenses = averageDailyExpenses,
                estimatedEndOfMonthBalance = estimatedEndOfMonthBalance,
                budgetStatus = budgetStatus,
                savingGoal = savingGoal,
                currentBalance = currentBalance,
                dailySavingsNeeded = dailySavingsNeeded,
                recommendedDailyExpenseBudget = recommendedDailyExpenseBudget,
                savingGoalStatus = savingGoalStatus,
                todayExpenses = todayExpenses
            )
        }
    }
}