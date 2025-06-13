package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrencySettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<CurrencySettings> {
        return settingsRepository.getCurrencySettings()
    }
}