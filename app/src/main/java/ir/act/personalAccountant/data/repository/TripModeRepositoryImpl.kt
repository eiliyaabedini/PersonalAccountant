package ir.act.personalAccountant.data.repository

import android.content.SharedPreferences
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.TripModeSettings
import ir.act.personalAccountant.domain.repository.TripModeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripModeRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : TripModeRepository {

    private val _tripModeSettings = MutableStateFlow(loadTripModeSettings())

    companion object {
        private const val KEY_TRIP_MODE_ENABLED = "trip_mode_enabled"
        private const val KEY_DESTINATION_CURRENCY_CODE = "destination_currency_code"
        private const val KEY_DESTINATION_CURRENCY_LOCALE = "destination_currency_locale"
        private const val KEY_EXCHANGE_RATE = "exchange_rate"
        private const val KEY_LAST_UPDATED = "last_updated"
    }

    override fun getTripModeSettings(): Flow<TripModeSettings> = _tripModeSettings.asStateFlow()

    override suspend fun updateTripModeSettings(settings: TripModeSettings) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_TRIP_MODE_ENABLED, settings.isEnabled)
            putString(KEY_DESTINATION_CURRENCY_CODE, settings.destinationCurrency.currencyCode)
            putString(KEY_DESTINATION_CURRENCY_LOCALE, settings.destinationCurrency.locale)
            putFloat(KEY_EXCHANGE_RATE, settings.exchangeRate.toFloat())
            putLong(KEY_LAST_UPDATED, settings.lastUpdated)
            apply()
        }
        _tripModeSettings.value = settings
    }

    override suspend fun enableTripMode(enabled: Boolean) {
        val currentSettings = _tripModeSettings.value
        updateTripModeSettings(currentSettings.copy(isEnabled = enabled))
    }

    override suspend fun updateExchangeRate(rate: Double) {
        val currentSettings = _tripModeSettings.value
        updateTripModeSettings(
            currentSettings.copy(
                exchangeRate = rate,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    private fun loadTripModeSettings(): TripModeSettings {
        return TripModeSettings(
            isEnabled = sharedPreferences.getBoolean(KEY_TRIP_MODE_ENABLED, false),
            destinationCurrency = CurrencySettings(
                currencyCode = sharedPreferences.getString(KEY_DESTINATION_CURRENCY_CODE, "USD")
                    ?: "USD",
                locale = sharedPreferences.getString(KEY_DESTINATION_CURRENCY_LOCALE, "en_US")
                    ?: "en_US"
            ),
            exchangeRate = sharedPreferences.getFloat(KEY_EXCHANGE_RATE, 1.0f).toDouble(),
            lastUpdated = sharedPreferences.getLong(KEY_LAST_UPDATED, System.currentTimeMillis())
        )
    }
}