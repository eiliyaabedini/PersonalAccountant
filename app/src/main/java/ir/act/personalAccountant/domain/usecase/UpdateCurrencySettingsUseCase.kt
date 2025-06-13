package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateCurrencySettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currencySettings: CurrencySettings) {
        settingsRepository.updateCurrencySettings(currencySettings)
    }
}