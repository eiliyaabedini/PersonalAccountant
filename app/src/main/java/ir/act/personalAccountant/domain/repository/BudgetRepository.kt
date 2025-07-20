package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.BudgetSettings
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetSettings(): Flow<BudgetSettings>
    suspend fun updateBudgetSettings(budgetSettings: BudgetSettings)
    suspend fun updateNetSalary(netSalary: Double)
    suspend fun updateTotalRent(totalRent: Double)
    suspend fun setBudgetConfigured(isConfigured: Boolean)
    suspend fun updateSavingGoal(savingGoal: Double)
}