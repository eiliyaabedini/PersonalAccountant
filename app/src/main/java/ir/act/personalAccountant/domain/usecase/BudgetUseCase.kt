package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.BudgetSettings
import ir.act.personalAccountant.domain.model.BudgetStatus
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
            
            val budgetStatus = when {
                totalExpensesWithRent > totalIncomeToDate -> BudgetStatus.RED
                totalExpensesWithRent > totalIncomeToDate * 0.8 -> BudgetStatus.MIDDLE
                else -> BudgetStatus.GOOD
            }
            
            BudgetData(
                currentDay = currentDay,
                totalDaysInMonth = totalDaysInMonth,
                dailyIncome = dailyIncome,
                dailyRent = dailyRent,
                totalIncomeToDate = totalIncomeToDate,
                totalRentToDate = totalRentToDate,
                totalExpensesToDate = totalExpensesToDate,
                budgetStatus = budgetStatus
            )
        }
    }
}