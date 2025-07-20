package ir.act.personalAccountant.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import ir.act.personalAccountant.domain.model.BudgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Cache @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val netSalaryKey = doublePreferencesKey("net_salary")
    private val totalRentKey = doublePreferencesKey("total_rent")
    private val isConfiguredKey = booleanPreferencesKey("is_budget_configured")
    private val monthlySavingGoalKey = doublePreferencesKey("monthly_saving_goal")

    fun getBudgetSettings(): Flow<BudgetSettings> {
        return dataStore.data.map { preferences ->
            BudgetSettings(
                netSalary = preferences[netSalaryKey] ?: 0.0,
                totalRent = preferences[totalRentKey] ?: 0.0,
                isConfigured = preferences[isConfiguredKey] ?: false,
                monthlySavingGoal = preferences[monthlySavingGoalKey] ?: 0.0
            )
        }
    }

    suspend fun getBudgetSettingsSync(): BudgetSettings {
        return getBudgetSettings().first()
    }

    suspend fun updateBudgetSettings(budgetSettings: BudgetSettings) {
        dataStore.edit { preferences ->
            preferences[netSalaryKey] = budgetSettings.netSalary
            preferences[totalRentKey] = budgetSettings.totalRent
            preferences[isConfiguredKey] = budgetSettings.isConfigured
            preferences[monthlySavingGoalKey] = budgetSettings.monthlySavingGoal
        }
    }
}