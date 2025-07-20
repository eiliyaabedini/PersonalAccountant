package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.TripModeSettings
import ir.act.personalAccountant.domain.repository.TripModeRepository
import javax.inject.Inject

class UpdateTripModeSettingsUseCase @Inject constructor(
    private val tripModeRepository: TripModeRepository
) {
    suspend operator fun invoke(settings: TripModeSettings) {
        tripModeRepository.updateTripModeSettings(settings)
    }
}