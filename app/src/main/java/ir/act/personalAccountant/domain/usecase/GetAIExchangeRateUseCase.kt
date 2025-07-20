package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.ai.AIEngine
import ir.act.personalAccountant.ai.data.repository.AIRepository
import ir.act.personalAccountant.domain.model.TripModeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAIExchangeRateUseCase @Inject constructor(
    private val aiEngine: AIEngine,
    private val aiRepository: AIRepository,
    private val updateTripModeSettingsUseCase: UpdateTripModeSettingsUseCase
) {
    suspend operator fun invoke(
        fromCurrency: String,
        toCurrency: String,
        currentTripModeSettings: TripModeSettings
    ): Flow<AIExchangeRateResult> = flow {
        try {
            emit(AIExchangeRateResult.Loading)

            // Get API key
            val apiKey = aiRepository.apiKey.first()
            if (apiKey.isEmpty()) {
                emit(
                    AIExchangeRateResult.Error(
                        "OpenAI API key not configured. Please go to Settings > AI Settings to set up your API key."
                    )
                )
                return@flow
            }

            // Fetch exchange rate using AI
            aiEngine.fetchCurrencyExchangeRate(fromCurrency, toCurrency, apiKey)
                .collect { response ->
                    val exchangeRate = response.exchangeRate
                    if (response.success && exchangeRate != null) {
                        // Update the trip mode settings with the new rate and timestamp
                        val updatedTripMode = currentTripModeSettings.copy(
                            exchangeRate = exchangeRate,
                            lastUpdated = response.timestamp ?: System.currentTimeMillis()
                        )

                        // Save to repository
                        updateTripModeSettingsUseCase(updatedTripMode)

                        emit(
                            AIExchangeRateResult.Success(
                                exchangeRate = exchangeRate,
                                updatedTripModeSettings = updatedTripMode,
                                source = response.source
                            )
                        )
                    } else {
                        emit(
                            AIExchangeRateResult.Error(
                                response.errorMessage ?: "Failed to fetch exchange rate"
                            )
                        )
                    }
                }

        } catch (e: Exception) {
            emit(
                AIExchangeRateResult.Error(
                    "Failed to fetch exchange rate: ${e.message}"
                )
            )
        }
    }
}

sealed class AIExchangeRateResult {
    object Loading : AIExchangeRateResult()
    data class Success(
        val exchangeRate: Double,
        val updatedTripModeSettings: TripModeSettings,
        val source: String?
    ) : AIExchangeRateResult()

    data class Error(val message: String) : AIExchangeRateResult()
}