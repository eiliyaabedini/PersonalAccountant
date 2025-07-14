package ir.act.personalAccountant.data.repository

import ir.act.personalAccountant.data.local.Cache
import ir.act.personalAccountant.domain.model.BudgetSettings
import ir.act.personalAccountant.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val cache: Cache
) : BudgetRepository {

    override fun getBudgetSettings(): Flow<BudgetSettings> {
        return cache.getBudgetSettings()
    }

    override suspend fun updateBudgetSettings(budgetSettings: BudgetSettings) {
        cache.updateBudgetSettings(budgetSettings)
    }

    override suspend fun updateNetSalary(netSalary: Double) {
        val currentSettings = cache.getBudgetSettingsSync()
        cache.updateBudgetSettings(currentSettings.copy(netSalary = netSalary))
    }

    override suspend fun updateTotalRent(totalRent: Double) {
        val currentSettings = cache.getBudgetSettingsSync()
        cache.updateBudgetSettings(currentSettings.copy(totalRent = totalRent))
    }

    override suspend fun setBudgetConfigured(isConfigured: Boolean) {
        val currentSettings = cache.getBudgetSettingsSync()
        cache.updateBudgetSettings(currentSettings.copy(isConfigured = isConfigured))
    }
}