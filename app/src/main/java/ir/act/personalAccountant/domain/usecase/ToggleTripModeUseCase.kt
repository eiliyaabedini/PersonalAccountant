package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.repository.TripModeRepository
import javax.inject.Inject

class ToggleTripModeUseCase @Inject constructor(
    private val tripModeRepository: TripModeRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        tripModeRepository.enableTripMode(enabled)
    }
}