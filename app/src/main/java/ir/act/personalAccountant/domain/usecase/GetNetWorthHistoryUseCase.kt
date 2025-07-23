package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.NetWorthSnapshot
import ir.act.personalAccountant.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNetWorthHistoryUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    operator fun invoke(fromDate: Long? = null): Flow<List<NetWorthSnapshot>> {
        return if (fromDate != null) {
            assetRepository.getNetWorthSnapshotsFromDate(fromDate)
        } else {
            assetRepository.getAllNetWorthSnapshots()
        }
    }
}