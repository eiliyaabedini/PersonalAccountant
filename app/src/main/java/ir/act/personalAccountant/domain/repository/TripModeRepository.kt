package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.TripModeSettings
import kotlinx.coroutines.flow.Flow

interface TripModeRepository {
    fun getTripModeSettings(): Flow<TripModeSettings>
    suspend fun updateTripModeSettings(settings: TripModeSettings)
    suspend fun enableTripMode(enabled: Boolean)
    suspend fun updateExchangeRate(rate: Double)
}