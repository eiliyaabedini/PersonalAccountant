package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.CurrencySettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCurrencySettings(): Flow<CurrencySettings>
    suspend fun updateCurrencySettings(currencySettings: CurrencySettings)
}