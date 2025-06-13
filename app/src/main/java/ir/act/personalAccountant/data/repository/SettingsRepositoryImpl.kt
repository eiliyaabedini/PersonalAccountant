package ir.act.personalAccountant.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val LOCALE = stringPreferencesKey("locale")
    }

    override fun getCurrencySettings(): Flow<CurrencySettings> {
        return dataStore.data.map { preferences ->
            CurrencySettings(
                currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: CurrencySettings.DEFAULT.currencyCode,
                locale = preferences[PreferencesKeys.LOCALE] ?: CurrencySettings.DEFAULT.locale
            )
        }
    }

    override suspend fun updateCurrencySettings(currencySettings: CurrencySettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = currencySettings.currencyCode
            preferences[PreferencesKeys.LOCALE] = currencySettings.locale
        }
    }
}