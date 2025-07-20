package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.TripModeSettings
import ir.act.personalAccountant.domain.repository.TripModeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripModeSettingsUseCase @Inject constructor(
    private val tripModeRepository: TripModeRepository
) {
    operator fun invoke(): Flow<TripModeSettings> {
        return tripModeRepository.getTripModeSettings()
    }
}