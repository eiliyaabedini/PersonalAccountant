package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.CurrencySettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCurrencySettings(): Flow<CurrencySettings>
    suspend fun updateCurrencySettings(currencySettings: CurrencySettings)

    // Google Sheets settings
    suspend fun saveStringSetting(key: String, value: String)
    suspend fun getStringSetting(key: String): String?
}